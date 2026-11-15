
package com.supportdesk.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.supportdesk.entity.Employee;
import com.supportdesk.entity.Role;
import com.supportdesk.repository.EmployeeRepository;
import com.supportdesk.security.JwtUtil;
import com.supportdesk.service.DuplicateEmailException;
import com.supportdesk.service.ResourceNotFoundException;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Integration and regression tests for GlobalExceptionHandler.
 * Covers error responses, HTTP status codes and sensitive data protection.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(GlobalExceptionHandlerTests.FaultSimulationController.class)
class GlobalExceptionHandlerTests
{
    @RestController
    @RequestMapping("/api/test-faults")
    public static class FaultSimulationController
    {
        @GetMapping("/server-error")
        public void triggerUnexpectedServerError()
        {
            throw new RuntimeException("Simulated unexpected crash: SELECT * FROM sensitive_db WHERE secret='XYZ'");
        }

        @GetMapping("/null-pointer")
        public void triggerNullPointer()
        {
            throw new NullPointerException("Simulated null pointer dereference");
        }

        @GetMapping("/type-mismatch/{id}")
        public ResponseEntity<String> triggerTypeMismatch(@PathVariable Long id)
        {
            return ResponseEntity.ok("id=" + id);
        }

        @GetMapping("/required-param")
        public ResponseEntity<String> triggerRequiredParam(@RequestParam String requiredKey)
        {
            return ResponseEntity.ok("key=" + requiredKey);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String employeeToken;
    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp()
    {
        exceptionHandler = new GlobalExceptionHandler();

        Employee admin = employeeRepository.findByEmail("admin.handler.test@supportdesk.com")
                .orElseGet(() -> employeeRepository.save(new Employee("Admin Handler", "admin.handler.test@supportdesk.com",
                        passwordEncoder.encode("Pass123"), "IT", Role.ADMIN, "1001")));
        adminToken = jwtUtil.generateToken(admin);

        Employee employee = employeeRepository.findByEmail("emp.handler.test@supportdesk.com")
                .orElseGet(() -> employeeRepository.save(new Employee("Emp Handler", "emp.handler.test@supportdesk.com",
                        passwordEncoder.encode("Pass123"), "HR", Role.EMPLOYEE, "1002")));
        employeeToken = jwtUtil.generateToken(employee);
    }

    // Direct unit tests for GlobalExceptionHandler.

    @Test
    @DisplayName("Unit: Unexpected server-side exception returns safe 500 without leaking stack traces or internal SQL")
    void testGeneralUnexpectedExceptionSanitizesResponse()
    {
        String sensitiveInternalMessage = "FATAL: Database connection leaked at org.internal.DbDriver table secret_accounts";
        RuntimeException ex = new RuntimeException(sensitiveInternalMessage);

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGeneralException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(500, body.get("status"));
        assertEquals("Internal Server Error", body.get("error"));
        assertEquals("An unexpected internal server error occurred. Please contact support or try again later.", body.get("message"));

        assertNull(body.get("stackTrace"));
        assertNull(body.get("trace"));
        assertFalse(body.toString().contains("secret_accounts"));
        assertFalse(body.toString().contains("DbDriver"));
    }

    @Test
    @DisplayName("Unit: General exception handler delegates AccessDeniedException to 403 Forbidden")
    void testGeneralExceptionDelegatesAccessDenied()
    {
        AccessDeniedException ex = new AccessDeniedException("Custom access forbidden message");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGeneralException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(403, body.get("status"));
        assertEquals("Forbidden", body.get("error"));
        assertEquals("Custom access forbidden message", body.get("message"));
    }

    @Test
    @DisplayName("Unit: General exception handler delegates AuthenticationException to 401 Unauthorized")
    void testGeneralExceptionDelegatesAuthenticationException()
    {
        BadCredentialsException ex = new BadCredentialsException("Invalid username or password");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGeneralException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(401, body.get("status"));
        assertEquals("Unauthorized", body.get("error"));
        assertEquals("Invalid username or password", body.get("message"));
    }

    @Test
    @DisplayName("Unit: General exception handler delegates ResourceNotFoundException to 404 Not Found")
    void testGeneralExceptionDelegatesResourceNotFound()
    {
        ResourceNotFoundException ex = new ResourceNotFoundException("Ticket #999 not found");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGeneralException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(404, body.get("status"));
        assertEquals("Not Found", body.get("error"));
        assertEquals("Ticket #999 not found", body.get("message"));
    }

    @Test
    @DisplayName("Unit: DataIntegrityViolationException returns 409 Conflict without leaking SQL details")
    void testDataIntegrityViolationSanitized()
    {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Duplicate entry 'test' for key 'employees.idx_email'");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleDataIntegrityViolation(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(409, body.get("status"));
        assertEquals("Conflict", body.get("error"));
        assertEquals("Database constraint violation: The operation could not be completed due to conflicting data.", body.get("message"));
        assertFalse(body.toString().contains("employees.idx_email"));
    }

    @Test
    @DisplayName("Unit: HttpMessageNotReadableException returns 400 Bad Request with safe message")
    void testHttpMessageNotReadable()
    {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("JSON parse error: Unexpected character",
                (org.springframework.http.HttpInputMessage) null);

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleHttpMessageNotReadable(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.get("status"));
        assertEquals("Bad Request", body.get("error"));
        assertEquals("Malformed JSON request or invalid request body.", body.get("message"));
    }

    @Test
    @DisplayName("Unit: MethodArgumentTypeMismatchException returns 400 Bad Request with parameter name")
    void testMethodArgumentTypeMismatch()
    {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException("abc", Long.class, "id", null, null);

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleMethodArgumentTypeMismatch(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.get("status"));
        assertEquals("Bad Request", body.get("error"));
        assertEquals("Invalid value for parameter 'id'. Expected type: Long.", body.get("message"));
    }

    @Test
    @DisplayName("Unit: MissingServletRequestParameterException returns 400 Bad Request with parameter name")
    void testMissingServletRequestParameter()
    {
        MissingServletRequestParameterException ex = new MissingServletRequestParameterException("searchKey", "String");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleMissingServletRequestParameter(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.get("status"));
        assertEquals("Bad Request", body.get("error"));
        assertEquals("Required request parameter 'searchKey' is missing.", body.get("message"));
    }

    @Test
    @DisplayName("Unit: HttpRequestMethodNotSupportedException returns 405 Method Not Allowed")
    void testHttpRequestMethodNotSupported()
    {
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("DELETE");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleMethodNotSupported(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(405, body.get("status"));
        assertEquals("Method Not Allowed", body.get("error"));
        assertEquals("HTTP method 'DELETE' is not supported for this endpoint.", body.get("message"));
    }

    @Test
    @DisplayName("Unit: HttpMediaTypeNotSupportedException returns 415 Unsupported Media Type")
    void testHttpMediaTypeNotSupported()
    {
        HttpMediaTypeNotSupportedException ex = new HttpMediaTypeNotSupportedException(
                MediaType.APPLICATION_XML, java.util.List.of(MediaType.APPLICATION_JSON));

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleMediaTypeNotSupported(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(415, body.get("status"));
        assertEquals("Unsupported Media Type", body.get("error"));
        assertEquals("Content type 'application/xml' is not supported.", body.get("message"));
    }

    @Test
    @DisplayName("Unit: NoResourceFoundException returns 404 Not Found")
    void testNoResourceFoundException()
    {
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/unknown/file.png", "File not found");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleNoResourceFound(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(404, body.get("status"));
        assertEquals("Not Found", body.get("error"));
        assertEquals("The requested resource was not found.", body.get("message"));
    }

    @Test
    @DisplayName("Unit: ResponseStatusException preserves custom status and reason")
    void testResponseStatusException()
    {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.I_AM_A_TEAPOT, "Custom teapot reason");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleResponseStatus(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.I_AM_A_TEAPOT, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(418, body.get("status"));
        assertEquals("I'm a teapot", body.get("error"));
        assertEquals("Custom teapot reason", body.get("message"));
    }

    // MockMvc integration tests.

    @Test
    @DisplayName("MockMvc: Unexpected server error returns 500 without leaking stack traces or SQL")
    void testMockMvcUnexpectedServerErrorReturnsSafe500() throws Exception
    {
        mockMvc.perform(get("/api/test-faults/server-error")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.error", is("Internal Server Error")))
                .andExpect(jsonPath("$.message", is("An unexpected internal server error occurred. Please contact support or try again later.")))
                .andExpect(jsonPath("$.stackTrace").doesNotExist())
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(content().string(not(containsString("sensitive_db"))))
                .andExpect(content().string(not(containsString("secret='XYZ'"))))
                .andExpect(content().string(not(containsString("RuntimeException"))));
    }

    @Test
    @DisplayName("MockMvc: NullPointerException returns safe 500")
    void testMockMvcNullPointerReturnsSafe500() throws Exception
    {
        mockMvc.perform(get("/api/test-faults/null-pointer")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.error", is("Internal Server Error")))
                .andExpect(jsonPath("$.message", is("An unexpected internal server error occurred. Please contact support or try again later.")))
                .andExpect(content().string(not(containsString("NullPointerException"))));
    }

    @Test
    @DisplayName("MockMvc: Method argument type mismatch returns 400 Bad Request")
    void testMockMvcTypeMismatchReturns400() throws Exception
    {
        mockMvc.perform(get("/api/test-faults/type-mismatch/not-a-number")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("Invalid value for parameter 'id'")));
    }

    @Test
    @DisplayName("MockMvc: Missing required request parameter returns 400 Bad Request")
    void testMockMvcMissingRequestParamReturns400() throws Exception
    {
        mockMvc.perform(get("/api/test-faults/required-param")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("Required request parameter 'requiredKey' is missing")));
    }

    @Test
    @DisplayName("MockMvc: Malformed JSON payload returns 400 Bad Request with clean message")
    void testMockMvcMalformedJsonReturns400() throws Exception
    {
        String brokenJson = "{\"title\": \"Incomplete JSON\", \"priority\":";

        mockMvc.perform(post("/api/tickets")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(brokenJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", is("Malformed JSON request or invalid request body.")));
    }

    @Test
    @DisplayName("MockMvc: Unsupported HTTP method returns 405 Method Not Allowed")
    void testMockMvcMethodNotAllowedReturns405() throws Exception
    {
        // GET is supported on this endpoint, but POST is not.
        mockMvc.perform(post("/api/employees/technicians")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status", is(405)))
                .andExpect(jsonPath("$.error", is("Method Not Allowed")))
                .andExpect(jsonPath("$.message", containsString("HTTP method 'POST' is not supported")));
    }

    @Test
    @DisplayName("MockMvc: Unauthenticated request returns 401 Unauthorized (not converted to 500)")
    void testMockMvcUnauthenticatedReturns401() throws Exception
    {
        mockMvc.perform(get("/api/test-faults/server-error"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")));
    }

    @Test
    @DisplayName("MockMvc: Forbidden request returns 403 Forbidden (not converted to 500)")
    void testMockMvcForbiddenReturns403() throws Exception
    {
        mockMvc.perform(get("/api/employees")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("Forbidden")));
    }

    @Test
    @DisplayName("MockMvc: ResourceNotFoundException returns 404 Not Found")
    void testMockMvcNotFoundReturns404() throws Exception
    {
        mockMvc.perform(get("/api/employees/999999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")));
    }

    @Test
    @DisplayName("MockMvc: MethodArgumentNotValidException returns 400 with validationErrors")
    void testMockMvcValidationErrorsReturn400() throws Exception
    {
        mockMvc.perform(post("/api/employees")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.validationErrors").exists());
    }
}
