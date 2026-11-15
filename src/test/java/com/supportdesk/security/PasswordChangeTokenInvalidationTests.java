package com.supportdesk.security;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.supportdesk.entity.Employee;
import com.supportdesk.entity.Role;
import com.supportdesk.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PasswordChangeTokenInvalidationTests
{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Employee admin;
    private String adminToken;

    @BeforeEach
    void setUp()
    {
        admin = new Employee("Admin User", "admin.inval@supportdesk.com", passwordEncoder.encode("AdminPass@123"), "IT", Role.ADMIN, "111");
        employeeRepository.save(admin);
        adminToken = jwtUtil.generateToken(admin);
    }

    @Test
    @DisplayName("Previously issued JWT is invalidated immediately after employee password change")
    void testOldTokenInvalidatedAfterPasswordChange() throws Exception
    {
        Employee employee = new Employee("Alice Walker", "alice.token@test.com", passwordEncoder.encode("OldPass@123"), "HR", Role.EMPLOYEE, "222");
        employeeRepository.save(employee);

        String initialToken = jwtUtil.generateToken(employee);

        mockMvc.perform(get("/api/employees/" + employee.getId())
                .header("Authorization", "Bearer " + initialToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("alice.token@test.com")));

        String updatePayload = """
                {
                    "name": "Alice Walker",
                    "email": "alice.token@test.com",
                    "department": "HR",
                    "role": "EMPLOYEE",
                    "phone": "222",
                    "password": "NewPass@456"
                }
                """;

        mockMvc.perform(put("/api/employees/" + employee.getId())
                .header("Authorization", "Bearer " + initialToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePayload))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/employees/" + employee.getId())
                .header("Authorization", "Bearer " + initialToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")));
    }

    @Test
    @DisplayName("Newly issued JWT works after password change while old token is rejected")
    void testNewTokenValidAfterPasswordChange() throws Exception
    {
        Employee employee = new Employee("Bob Stone", "bob.token@test.com", passwordEncoder.encode("OldPass@123"), "Operations", Role.EMPLOYEE, "333");
        employeeRepository.save(employee);

        String oldToken = jwtUtil.generateToken(employee);

        String updatePayload = """
                {
                    "name": "Bob Stone",
                    "email": "bob.token@test.com",
                    "department": "Operations",
                    "role": "EMPLOYEE",
                    "phone": "333",
                    "password": "NewBobPass@789"
                }
                """;

        mockMvc.perform(put("/api/employees/" + employee.getId())
                .header("Authorization", "Bearer " + oldToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePayload))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/employees/" + employee.getId())
                .header("Authorization", "Bearer " + oldToken))
                .andExpect(status().isUnauthorized());

        String loginPayload = """
                {
                    "email": "bob.token@test.com",
                    "password": "NewBobPass@789"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        JsonNode responseNode = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String newToken = responseNode.get("token").asText();

        mockMvc.perform(get("/api/employees/" + employee.getId())
                .header("Authorization", "Bearer " + newToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Bob Stone")));
    }

    @Test
    @DisplayName("Password change for one employee does not invalidate tokens of other employees")
    void testOtherUsersTokensUnaffectedByPasswordChange() throws Exception
    {
        Employee userA = new Employee("User Alpha", "alpha@test.com", passwordEncoder.encode("AlphaPass@123"), "Support", Role.EMPLOYEE, "444");
        Employee userB = new Employee("User Beta", "beta@test.com", passwordEncoder.encode("BetaPass@123"), "Support", Role.EMPLOYEE, "555");
        employeeRepository.save(userA);
        employeeRepository.save(userB);

        String tokenA = jwtUtil.generateToken(userA);
        String tokenB = jwtUtil.generateToken(userB);

        mockMvc.perform(get("/api/employees/" + userA.getId())
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/employees/" + userB.getId())
                .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk());

        String updatePayload = """
                {
                    "name": "User Alpha",
                    "email": "alpha@test.com",
                    "department": "Support",
                    "role": "EMPLOYEE",
                    "phone": "444",
                    "password": "NewAlphaPass@456"
                }
                """;

        mockMvc.perform(put("/api/employees/" + userA.getId())
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePayload))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/employees/" + userA.getId())
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/employees/" + userB.getId())
                .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("beta@test.com")));
    }

    @Test
    @DisplayName("Failed password update does not invalidate existing valid token")
    void testFailedPasswordChangeDoesNotInvalidateToken() throws Exception
    {
        Employee employee = new Employee("Charlie Brown", "charlie@test.com", passwordEncoder.encode("ValidPass@123"), "Finance", Role.EMPLOYEE, "666");
        employeeRepository.save(employee);

        String token = jwtUtil.generateToken(employee);

        String invalidPayload = """
                {
                    "name": "Charlie Brown",
                    "email": "charlie@test.com",
                    "department": "Finance",
                    "role": "EMPLOYEE",
                    "phone": "666",
                    "password": "123"
                }
                """;

        mockMvc.perform(put("/api/employees/" + employee.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPayload))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/employees/" + employee.getId())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("charlie@test.com")));
    }

    @Test
    @DisplayName("Profile update without password change does not invalidate existing token")
    void testProfileUpdateWithoutPasswordDoesNotInvalidateToken() throws Exception
    {
        Employee employee = new Employee("Diana Prince", "diana@test.com", passwordEncoder.encode("DianaPass@123"), "Marketing", Role.EMPLOYEE, "777");
        employeeRepository.save(employee);

        String token = jwtUtil.generateToken(employee);

        String updatePayload = """
                {
                    "name": "Diana Prince Updated",
                    "email": "diana@test.com",
                    "department": "Executive",
                    "role": "EMPLOYEE",
                    "phone": "9998887776",
                    "password": ""
                }
                """;

        mockMvc.perform(put("/api/employees/" + employee.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Diana Prince Updated")))
                .andExpect(jsonPath("$.department", is("Executive")));

        mockMvc.perform(get("/api/employees/" + employee.getId())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Diana Prince Updated")));
    }

    @Test
    @DisplayName("Deactivated employee token is rejected even if password hash matches")
    void testDeactivatedUserTokenRejected() throws Exception
    {
        Employee employee = new Employee("Evan Ross", "evan@test.com", passwordEncoder.encode("EvanPass@123"), "Facilities", Role.EMPLOYEE, "888");
        employeeRepository.save(employee);

        String token = jwtUtil.generateToken(employee);

        mockMvc.perform(patch("/api/employees/" + employee.getId() + "/deactivate")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/employees/" + employee.getId())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));
    }
}