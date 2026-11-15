package com.supportdesk.security;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.supportdesk.entity.Employee;
import com.supportdesk.entity.Role;
import com.supportdesk.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

class CorsConfigurationTests
{
    @Nested
    @SpringBootTest
    @AutoConfigureMockMvc
    @Transactional
    class StandardDevCorsTests
    {
        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private EmployeeRepository employeeRepository;

        @Autowired
        private JwtUtil jwtUtil;

        @Autowired
        private PasswordEncoder passwordEncoder;

        private String adminToken;

        @BeforeEach
        void setUp()
        {
            Employee admin = employeeRepository.findByEmail("admin.cors.test@supportdesk.com")
                    .orElseGet(() -> employeeRepository.save(new Employee(
                            "Admin Cors", "admin.cors.test@supportdesk.com",
                            passwordEncoder.encode("Pass123"), "IT", Role.ADMIN, "5001"
                    )));
            adminToken = jwtUtil.generateToken(admin);
        }

        @Test
        @DisplayName("CORS Preflight: Permitted local origin receives 200 OK and correct CORS headers")
        void testPreflightPermittedOrigin() throws Exception
        {
            mockMvc.perform(options("/api/tickets")
                            .header(HttpHeaders.ORIGIN, "http://localhost:8082")
                            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization, Content-Type"))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:8082"))
                    .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"))
                    .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, containsString("POST")))
                    .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, containsString("Authorization")));
        }

        @Test
        @DisplayName("CORS Preflight: Untrusted origin is rejected with 403 Forbidden")
        void testPreflightUntrustedOriginForbidden() throws Exception
        {
            mockMvc.perform(options("/api/tickets")
                            .header(HttpHeaders.ORIGIN, "http://untrusted-malicious-domain.com")
                            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization, Content-Type"))
                    .andExpect(status().isForbidden())
                    .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        }

        @Test
        @DisplayName("CORS Preflight: Permitted frontend dev port (3000) receives 200 OK")
        void testPreflightFrontendDevPort3000() throws Exception
        {
            mockMvc.perform(options("/api/dashboard/summary")
                            .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000"))
                    .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
        }

        @Test
        @DisplayName("CORS Preflight: Permitted frontend dev port (5173) receives 200 OK")
        void testPreflightFrontendDevPort5173() throws Exception
        {
            mockMvc.perform(options("/api/dashboard/summary")
                            .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));
        }

        @Test
        @DisplayName("CORS Actual: Actual POST /api/auth/login from permitted origin receives CORS headers")
        void testActualRequestPermittedOriginReceivesCorsHeaders() throws Exception
        {
            String loginPayload = """
                    {
                        "email": "admin.cors.test@supportdesk.com",
                        "password": "Pass123"
                    }
                    """;

            mockMvc.perform(post("/api/auth/login")
                            .header(HttpHeaders.ORIGIN, "http://localhost:8082")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginPayload))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:8082"))
                    .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"))
                    .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, containsString("Authorization")))
                    .andExpect(jsonPath("$.token").exists());
        }

        @Test
        @DisplayName("CORS Actual: Actual request from untrusted origin does NOT receive Access-Control-Allow-Origin")
        void testActualRequestUntrustedOriginNoCorsHeader() throws Exception
        {
            mockMvc.perform(get("/api/dashboard/summary")
                            .header(HttpHeaders.ORIGIN, "http://untrusted-malicious-domain.com")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        }

        @Test
        @DisplayName("Same-origin: Request without Origin header operates normally")
        void testSameOriginWithoutOriginHeaderWorksNormally() throws Exception
        {
            mockMvc.perform(get("/api/dashboard/summary")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
                    .andExpect(jsonPath("$.totalTickets").exists());
        }

        @Test
        @DisplayName("CORS Preflight: PATCH method preflight works for permitted origins")
        void testPreflightPatchMethod() throws Exception
        {
            mockMvc.perform(options("/api/tickets/1/status")
                            .header(HttpHeaders.ORIGIN, "http://localhost:8082")
                            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "PATCH")
                            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization, Content-Type"))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, containsString("PATCH")));
        }
    }

    @Nested
    @SpringBootTest
    @AutoConfigureMockMvc
    @Transactional
    @TestPropertySource(properties = "app.cors.allowed-origins=https://custom-portal.company.com")
    class ExplicitCustomOriginTests
    {
        @Autowired
        private MockMvc mockMvc;

        @Test
        @DisplayName("Custom configured origin receives CORS approval")
        void testExplicitCustomOriginAllowed() throws Exception
        {
            mockMvc.perform(options("/api/tickets")
                            .header(HttpHeaders.ORIGIN, "https://custom-portal.company.com")
                            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://custom-portal.company.com"))
                    .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
        }

        @Test
        @DisplayName("When custom origin is configured, localhost is untrusted and rejected")
        void testLocalhostRejectedWhenCustomOriginConfigured() throws Exception
        {
            mockMvc.perform(options("/api/tickets")
                            .header(HttpHeaders.ORIGIN, "http://localhost:8082")
                            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                    .andExpect(status().isForbidden())
                    .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        }
    }

    @Nested
    @SpringBootTest
    @AutoConfigureMockMvc
    @Transactional
    @TestPropertySource(properties = "app.cors.allowed-origins=")
    class EmptyOriginDisallowsAllCorsTests
    {
        @Autowired
        private MockMvc mockMvc;

        @Test
        @DisplayName("When allowed origins is empty, all cross-origin preflight requests are rejected")
        void testEmptyAllowedOriginsRejectsAll() throws Exception
        {
            mockMvc.perform(options("/api/tickets")
                            .header(HttpHeaders.ORIGIN, "http://localhost:8082")
                            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                    .andExpect(status().isForbidden())
                    .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        }
    }
}