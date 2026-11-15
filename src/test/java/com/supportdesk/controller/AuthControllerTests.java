
package com.supportdesk.controller;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.supportdesk.entity.Employee;
import com.supportdesk.entity.Role;
import com.supportdesk.repository.EmployeeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTests 
{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("POST /api/auth/login - Successfully authenticates active employee and returns JWT without passwords")
    void testLoginSuccess() throws Exception {
        Employee employee = new Employee("Ananya Sen", "ananya@test.com", passwordEncoder.encode("SecurePass@123"), "IT", Role.TECHNICIAN, "987654");
        employeeRepository.save(employee);

        String loginPayload = """
                {
                    "email": "ananya@test.com",
                    "password": "SecurePass@123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.type", is("Bearer")))
                .andExpect(jsonPath("$.email", is("ananya@test.com")))
                .andExpect(jsonPath("$.name", is("Ananya Sen")))
                .andExpect(jsonPath("$.role", is("TECHNICIAN")))
                .andExpect(jsonPath("$.id", is(employee.getId().intValue())))
                .andExpect(jsonPath("$", not(hasKey("password"))));
    }

    @Test
    @DisplayName("POST /api/auth/login - Rejects invalid password with 401 Unauthorized")
    void testLoginInvalidPasswordFails() throws Exception {
        Employee employee = new Employee("Brijesh Roy", "brijesh@test.com", passwordEncoder.encode("RealPassword123"), "HR", Role.EMPLOYEE, "112233");
        employeeRepository.save(employee);

        String wrongPasswordPayload = """
                {
                    "email": "brijesh@test.com",
                    "password": "WrongPassword999"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(wrongPasswordPayload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")))
                .andExpect(jsonPath("$.message", is("Invalid email or password.")));
    }

    @Test
    @DisplayName("POST /api/auth/login - Rejects non-existent email with 401 Unauthorized")
    void testLoginNonExistentEmailFails() throws Exception {
        String missingEmailPayload = """
                {
                    "email": "ghost.user@test.com",
                    "password": "SomePassword123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(missingEmailPayload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")));
    }

    @Test
    @DisplayName("POST /api/auth/login - Rejects deactivated employee with 401 Unauthorized")
    void testLoginInactiveAccountFails() throws Exception {
        Employee inactiveUser = new Employee("Inactive User", "inactive.login@test.com", passwordEncoder.encode("Pass12345"), "Finance", Role.EMPLOYEE, "445566");
        inactiveUser.setActive(false);
        employeeRepository.save(inactiveUser);

        String loginPayload = """
                {
                    "email": "inactive.login@test.com",
                    "password": "Pass12345"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginPayload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.message", is("Account is deactivated. Please contact an administrator.")));
    }

    @Test
    @DisplayName("POST /api/auth/login - Returns 400 Bad Request on blank credentials")
    void testLoginValidationFailure() throws Exception {
        String blankPayload = """
                {
                    "email": "",
                    "password": ""
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(blankPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.validationErrors.email").exists())
                .andExpect(jsonPath("$.validationErrors.password").exists());
    }
}
