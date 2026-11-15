
package com.supportdesk.config;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
class DevelopmentDataInitializerTests
{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DevelopmentDataInitializer dataInitializer;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Verify initializer seeds admin, tech, and staff accounts with BCrypt passwords")
    void testInitializationSeedsAllThreeAccounts() {
        dataInitializer.run();

        // Admin account
        Employee admin = employeeRepository.findByEmail("admin.main@supportdesk.com").orElse(null);
        assertNotNull(admin, "Admin account should be seeded");
        assertTrue(admin.isActive());
        assertTrue(admin.getRole() == Role.ADMIN);
        assertFalse("Pass123".equals(admin.getPassword()), "Password must not be stored in plaintext");
        assertTrue(passwordEncoder.matches("Pass123", admin.getPassword()), "BCrypt password must match 'Pass123'");

        // Technician account
        Employee tech = employeeRepository.findByEmail("tech.user@supportdesk.com").orElse(null);
        assertNotNull(tech, "Tech account should be seeded");
        assertTrue(tech.isActive());
        assertTrue(tech.getRole() == Role.TECHNICIAN);
        assertTrue(passwordEncoder.matches("Pass123", tech.getPassword()));

        // Staff account
        Employee staff = employeeRepository.findByEmail("staff.user@supportdesk.com").orElse(null);
        assertNotNull(staff, "Staff account should be seeded");
        assertTrue(staff.isActive());
        assertTrue(staff.getRole() == Role.EMPLOYEE);
        assertTrue(passwordEncoder.matches("Pass123", staff.getPassword()));
    }

    @Test
    @DisplayName("Verify initializer is idempotent and safe against repeated execution")
    void testInitializationIdempotency() {
        dataInitializer.run();
        long countAfterFirstRun = employeeRepository.count();

        dataInitializer.run();
        long countAfterSecondRun = employeeRepository.count();

        assertTrue(countAfterFirstRun == countAfterSecondRun, "Repeated execution must not insert duplicate accounts");
    }

    @Test
    @DisplayName("Verify existing account details and custom passwords are preserved without overwriting")
    void testExistingAccountPreservation() {
        String testEmail = "custom.admin@supportdesk.com";
        Employee custom = new Employee("Original Custom Admin", testEmail, passwordEncoder.encode("CustomSecretPass999"), "Finance", Role.ADMIN, "555");
        employeeRepository.save(custom);

        dataInitializer.seedAccountIfNotExists("Overwritten Name", testEmail, "NewPass123", "IT", Role.EMPLOYEE, "000");

        Employee preserved = employeeRepository.findByEmail(testEmail).orElseThrow();
        assertTrue("Original Custom Admin".equals(preserved.getName()), "Existing name must be preserved");
        assertTrue("Finance".equals(preserved.getDepartment()), "Existing department must be preserved");
        assertTrue(passwordEncoder.matches("CustomSecretPass999", preserved.getPassword()), "Original password must be preserved");
        assertFalse(passwordEncoder.matches("NewPass123", preserved.getPassword()), "Password must NOT be overwritten");
    }

    @Test
    @DisplayName("End-to-end Login: POST /api/auth/login succeeds for all seeded test accounts")
    void testLoginWithSeededAccounts() throws Exception {
        dataInitializer.run();

        String adminLoginPayload = """
                {
                    "email": "admin.main@supportdesk.com",
                    "password": "Pass123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adminLoginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email", is("admin.main@supportdesk.com")))
                .andExpect(jsonPath("$.role", is("ADMIN")));

        String techLoginPayload = """
                {
                    "email": "tech.user@supportdesk.com",
                    "password": "Pass123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(techLoginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email", is("tech.user@supportdesk.com")))
                .andExpect(jsonPath("$.role", is("TECHNICIAN")));

        String staffLoginPayload = """
                {
                    "email": "staff.user@supportdesk.com",
                    "password": "Pass123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(staffLoginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email", is("staff.user@supportdesk.com")))
                .andExpect(jsonPath("$.role", is("EMPLOYEE")));

        String badPassPayload = """
                {
                    "email": "admin.main@supportdesk.com",
                    "password": "WrongPassword!"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badPassPayload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.message", is("Invalid email or password.")));
    }
}
