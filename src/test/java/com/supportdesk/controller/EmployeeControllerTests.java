
package com.supportdesk.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.supportdesk.entity.Employee;
import com.supportdesk.entity.Role;
import com.supportdesk.repository.EmployeeRepository;
import com.supportdesk.security.JwtUtil;
import com.supportdesk.service.AuthService;
import com.supportdesk.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for employee APIs using MockMvc.
 * Covers employee operations, validation, access control and password security.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EmployeeControllerTests
{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;

    @BeforeEach
    void setUp()
    {
        Employee admin = new Employee("Admin Main", "admin.emp.test@supportdesk.com", "password123", "IT", Role.ADMIN, "000");
        employeeRepository.save(admin);
        adminToken = jwtUtil.generateToken(admin);
    }

    @Test
    @DisplayName("POST /api/employees - Creates employee and NEVER returns password in JSON")
    void testCreateEmployeeWithoutPasswordInResponse() throws Exception
    {
        String jsonPayload = """
                {
                    "name": "Pooja Verma",
                    "email": "pooja.verma@supportdesk.com",
                    "password": "Secret@123",
                    "department": "IT",
                    "role": "TECHNICIAN",
                    "phone": "9871112223"
                }
                """;

        mockMvc.perform(post("/api/employees")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name", is("Pooja Verma")))
                .andExpect(jsonPath("$.email", is("pooja.verma@supportdesk.com")))
                .andExpect(jsonPath("$.role", is("TECHNICIAN")))
                .andExpect(jsonPath("$.active", is(true)))
                // Passwords must never be included in API responses.
                .andExpect(jsonPath("$", not(hasKey("password"))));
    }

    @Test
    @DisplayName("POST /api/employees - Returns 400 Bad Request on validation failure (blank name, invalid email)")
    void testCreateEmployeeValidationFailure() throws Exception
    {
        String invalidPayload = """
                {
                    "name": "",
                    "email": "not-an-email",
                    "password": "123",
                    "department": "",
                    "role": null
                }
                """;

        mockMvc.perform(post("/api/employees")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.email").exists());
    }

    @Test
    @DisplayName("POST /api/employees - Returns 409 Conflict on duplicate email")
    void testCreateEmployeeDuplicateEmail() throws Exception
    {
        Employee existing = new Employee("Existing User", "duplicate.api@test.com", "pass1234", "HR", Role.EMPLOYEE, "111");
        employeeService.createEmployee(existing);

        String duplicatePayload = """
                {
                    "name": "Second User",
                    "email": "duplicate.api@test.com",
                    "password": "pass5678",
                    "department": "Sales",
                    "role": "EMPLOYEE",
                    "phone": "222"
                }
                """;

        mockMvc.perform(post("/api/employees")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(duplicatePayload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Conflict")))
                .andExpect(jsonPath("$.message", containsString("already registered")));
    }

    @Test
    @DisplayName("GET /api/employees - Lists all employees without passwords")
    void testGetAllEmployees() throws Exception
    {
        Employee emp = new Employee("Ramesh Kumar", "ramesh@test.com", "pass1234", "Finance", Role.EMPLOYEE, "444");
        employeeService.createEmployee(emp);

        mockMvc.perform(get("/api/employees")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]", not(hasKey("password"))));
    }

    @Test
    @DisplayName("GET /api/employees/{id} - Returns employee by ID or 404 when not found")
    void testGetEmployeeById() throws Exception
    {
        Employee emp = new Employee("Suresh Patel", "suresh@test.com", "pass1234", "Operations", Role.EMPLOYEE, "555");
        Employee saved = employeeService.createEmployee(emp);

        mockMvc.perform(get("/api/employees/" + saved.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Suresh Patel")))
                .andExpect(jsonPath("$", not(hasKey("password"))));

        mockMvc.perform(get("/api/employees/999999")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")));
    }

    @Test
    @DisplayName("GET /api/employees/technicians - Lists only technicians")
    void testGetTechnicians() throws Exception
    {
        Employee tech = new Employee("Tech Lead", "techlead@test.com", "pass1234", "IT", Role.TECHNICIAN, "777");
        employeeService.createEmployee(tech);

        mockMvc.perform(get("/api/employees/technicians")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].role", is("TECHNICIAN")));
    }

    @Test
    @DisplayName("PUT /api/employees/{id} - Updates employee details")
    void testUpdateEmployee() throws Exception
    {
        Employee emp = new Employee("Old Name", "old.email@test.com", "pass1234", "IT", Role.EMPLOYEE, "123");
        Employee saved = employeeService.createEmployee(emp);

        String updatePayload = """
                {
                    "name": "Updated Name",
                    "email": "updated.email@test.com",
                    "password": "newpass123",
                    "department": "IT Helpdesk",
                    "role": "TECHNICIAN",
                    "phone": "999"
                }
                """;

        mockMvc.perform(put("/api/employees/" + saved.getId())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Name")))
                .andExpect(jsonPath("$.email", is("updated.email@test.com")))
                .andExpect(jsonPath("$.department", is("IT Helpdesk")))
                .andExpect(jsonPath("$.role", is("TECHNICIAN")))
                .andExpect(jsonPath("$", not(hasKey("password"))));
    }

    @Test
    @DisplayName("PATCH /api/employees/{id}/deactivate - Sets active = false")
    void testDeactivateEmployee() throws Exception
    {
        Employee emp = new Employee("To Deactivate", "deact@test.com", "pass1234", "HR", Role.EMPLOYEE, "123");
        Employee saved = employeeService.createEmployee(emp);

        mockMvc.perform(patch("/api/employees/" + saved.getId() + "/deactivate")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active", is(false)));
    }

    @Test
    @DisplayName("DELETE /api/employees/{id} - Soft deletes employee (active = false)")
    void testDeleteEmployeeSoftDelete() throws Exception
    {
        Employee emp = new Employee("To Delete", "delete@test.com", "pass1234", "Sales", Role.EMPLOYEE, "123");
        Employee saved = employeeService.createEmployee(emp);

        mockMvc.perform(delete("/api/employees/" + saved.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active", is(false)));
    }

    @Test
    @DisplayName("PUT /api/employees/{id} - Blank password preserves existing hash and authenticates with old password")
    void testUpdateEmployeeWithBlankPasswordPreservesOldPasswordAndAuthenticates() throws Exception
    {
        Employee emp = new Employee("Profile Only", "profile.blank@test.com", "OriginalPass123", "Support", Role.EMPLOYEE, "111");
        Employee saved = employeeService.createEmployee(emp);
        String originalHash = saved.getPassword();

        // An empty password should leave the existing password unchanged.
        String updatePayload = """
                {
                    "name": "Profile Only Renamed",
                    "email": "profile.blank@test.com",
                    "password": "",
                    "department": "Senior Support",
                    "role": "EMPLOYEE",
                    "phone": "222"
                }
                """;

        mockMvc.perform(put("/api/employees/" + saved.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Profile Only Renamed")))
                .andExpect(jsonPath("$.department", is("Senior Support")))
                .andExpect(jsonPath("$", not(hasKey("password"))));

        // Check that the stored password hash has not changed.
        Employee reloaded = employeeRepository.findById(saved.getId()).orElseThrow();
        assertEquals(originalHash, reloaded.getPassword(), "Encoded password hash must remain unchanged when password is blank");

        // The employee should still be able to log in with the original password.
        LoginResponse loginResp = authService.login(new LoginRequest("profile.blank@test.com", "OriginalPass123"));
        assertTrue(loginResp.getToken() != null && !loginResp.getToken().isBlank(), "Original password must still authenticate");
    }

    @Test
    @DisplayName("PUT /api/employees/{id} - Omitted password preserves existing hash and authenticates")
    void testUpdateEmployeeWithOmittedPasswordPreservesOldPasswordAndAuthenticates() throws Exception
    {
        Employee emp = new Employee("Omitted Pwd", "profile.omitted@test.com", "OriginalPass123", "QA", Role.TECHNICIAN, "333");
        Employee saved = employeeService.createEmployee(emp);
        String originalHash = saved.getPassword();

        // The password field is intentionally left out of the update request.
        String updatePayload = """
                {
                    "name": "Omitted Pwd Renamed",
                    "email": "profile.omitted@test.com",
                    "department": "Senior QA",
                    "role": "TECHNICIAN",
                    "phone": "444"
                }
                """;

        mockMvc.perform(put("/api/employees/" + saved.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Omitted Pwd Renamed")))
                .andExpect(jsonPath("$.department", is("Senior QA")))
                .andExpect(jsonPath("$", not(hasKey("password"))));

        Employee reloaded = employeeRepository.findById(saved.getId()).orElseThrow();
        assertEquals(originalHash, reloaded.getPassword(), "Encoded password hash must remain unchanged when password is omitted");

        // Existing credentials should continue to work after the profile update.
        LoginResponse loginResp = authService.login(new LoginRequest("profile.omitted@test.com", "OriginalPass123"));
        assertTrue(loginResp.getToken() != null && !loginResp.getToken().isBlank(), "Original password must authenticate");
    }

    @Test
    @DisplayName("PUT /api/employees/{id} - Valid new password updates hash and authenticates with new password")
    void testUpdateEmployeeWithValidNewPasswordUpdatesHashAndAuthenticates() throws Exception
    {
        Employee emp = new Employee("Change Pwd", "change.pwd@test.com", "InitialSecret123", "Dev", Role.EMPLOYEE, "555");
        Employee saved = employeeService.createEmployee(emp);
        String initialHash = saved.getPassword();

        // A valid new password should replace the existing password.
        String updatePayload = """
                {
                    "name": "Change Pwd",
                    "email": "change.pwd@test.com",
                    "password": "BrandNewPassword789",
                    "department": "Dev",
                    "role": "EMPLOYEE",
                    "phone": "555"
                }
                """;

        mockMvc.perform(put("/api/employees/" + saved.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(hasKey("password"))));

        Employee reloaded = employeeRepository.findById(saved.getId()).orElseThrow();
        assertNotEquals(initialHash, reloaded.getPassword(), "Password hash must change when new password is provided");
        assertTrue(passwordEncoder.matches("BrandNewPassword789", reloaded.getPassword()), "New password must match BCrypt hash");

        // The old password should no longer work.
        assertThrows(BadCredentialsException.class, () ->
                authService.login(new LoginRequest("change.pwd@test.com", "InitialSecret123")));

        // The new password should allow the employee to log in.
        LoginResponse loginResp = authService.login(new LoginRequest("change.pwd@test.com", "BrandNewPassword789"));
        assertTrue(loginResp.getToken() != null && !loginResp.getToken().isBlank(), "New password must authenticate");
    }

    @Test
    @DisplayName("PUT /api/employees/{id} - Short invalid new password is rejected with 400 Bad Request")
    void testUpdateEmployeeWithInvalidPasswordRejected() throws Exception
    {
        Employee emp = new Employee("Invalid Pwd", "invalid.pwd@test.com", "InitialSecret123", "Ops", Role.EMPLOYEE, "666");
        Employee saved = employeeService.createEmployee(emp);

        // Passwords shorter than six characters should be rejected.
        String updatePayload = """
                {
                    "name": "Invalid Pwd",
                    "email": "invalid.pwd@test.com",
                    "password": "123",
                    "department": "Ops",
                    "role": "EMPLOYEE",
                    "phone": "666"
                }
                """;

        mockMvc.perform(put("/api/employees/" + saved.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));

        // A rejected update must not change the existing password.
        Employee reloaded = employeeRepository.findById(saved.getId()).orElseThrow();
        assertTrue(passwordEncoder.matches("InitialSecret123", reloaded.getPassword()), "Original password must be preserved on rejected update");
    }

    @Test
    @DisplayName("POST /api/employees - Creation still requires a valid password (cannot be empty)")
    void testCreateEmployeeRequiresPassword() throws Exception
    {
        String invalidCreatePayload = """
                {
                    "name": "No Password User",
                    "email": "nopass@test.com",
                    "password": "",
                    "department": "Ops",
                    "role": "EMPLOYEE",
                    "phone": "777"
                }
                """;

        mockMvc.perform(post("/api/employees")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidCreatePayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.validationErrors.password", containsString("Password")));
    }

    @Test
    @DisplayName("PUT /api/employees/{id} - Non-admin cannot elevate own role")
    void testNonAdminCannotElevateRoleOnUpdate() throws Exception
    {
        Employee staff = new Employee("Normal Staff", "normal.staff@test.com", "Pass12345", "Ops", Role.EMPLOYEE, "888");
        Employee savedStaff = employeeService.createEmployee(staff);
        String staffToken = jwtUtil.generateToken(savedStaff);

        String elevatePayload = """
                {
                    "name": "Normal Staff",
                    "email": "normal.staff@test.com",
                    "department": "Ops",
                    "role": "ADMIN",
                    "phone": "888"
                }
                """;

        mockMvc.perform(put("/api/employees/" + savedStaff.getId())
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(elevatePayload))
                .andExpect(status().isOk());

        Employee reloaded = employeeRepository.findById(savedStaff.getId()).orElseThrow();
        assertEquals(Role.EMPLOYEE, reloaded.getRole(), "Role must remain EMPLOYEE; elevation to ADMIN must be ignored");
    }

    @Test
    @DisplayName("PUT /api/employees/{id} - Non-admin cannot update other employee profiles (403 Forbidden)")
    void testNonAdminCannotUpdateOtherEmployee() throws Exception
    {
        Employee staff1 = new Employee("Staff One", "staff1@test.com", "Pass12345", "Ops", Role.EMPLOYEE, "111");
        Employee savedStaff1 = employeeService.createEmployee(staff1);
        String staff1Token = jwtUtil.generateToken(savedStaff1);

        Employee staff2 = new Employee("Staff Two", "staff2@test.com", "Pass12345", "Ops", Role.EMPLOYEE, "222");
        Employee savedStaff2 = employeeService.createEmployee(staff2);

        String updatePayload = """
                {
                    "name": "Tampered Name",
                    "email": "staff2@test.com",
                    "department": "Ops",
                    "role": "EMPLOYEE",
                    "phone": "222"
                }
                """;

        mockMvc.perform(put("/api/employees/" + savedStaff2.getId())
                        .header("Authorization", "Bearer " + staff1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    @Test
    @DisplayName("PATCH /api/employees/{id}/deactivate - Admin cannot deactivate self (400 Bad Request)")
    void testAdminCannotSelfDeactivate() throws Exception
    {
        Employee admin = employeeRepository.findByEmail("admin.emp.test@supportdesk.com").orElseThrow();

        mockMvc.perform(patch("/api/employees/" + admin.getId() + "/deactivate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("Administrators cannot deactivate their own account")));
    }
}
