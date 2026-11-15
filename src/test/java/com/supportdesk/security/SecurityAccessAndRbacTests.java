package com.supportdesk.security;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.supportdesk.controller.CreateTicketRequest;
import com.supportdesk.entity.Employee;
import com.supportdesk.entity.Role;
import com.supportdesk.entity.Ticket;
import com.supportdesk.entity.TicketPriority;
import com.supportdesk.repository.EmployeeRepository;
import com.supportdesk.service.TicketService;
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
class SecurityAccessAndRbacTests
{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Unauthenticated request to protected endpoint returns 401 Unauthorized")
    void testUnauthenticatedRequestReturns401() throws Exception
    {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")));
    }

    @Test
    @DisplayName("Invalid or tampered JWT token returns 401 Unauthorized")
    void testInvalidTokenReturns401() throws Exception
    {
        mockMvc.perform(get("/api/employees")
                .header("Authorization", "Bearer invalid.tampered.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")));
    }

    @Test
    @DisplayName("Valid JWT token grants access to authorized endpoint")
    void testValidJwtGrantsAccess() throws Exception
    {
        Employee admin = employeeRepository.findByEmail("admin.main@supportdesk.com")
                .orElseGet(() -> employeeRepository.save(new Employee("Admin Main", "admin.main@supportdesk.com", passwordEncoder.encode("Pass123"), "IT", Role.ADMIN, "123")));

        String token = jwtUtil.generateToken(admin);

        mockMvc.perform(get("/api/employees")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("EMPLOYEE role is forbidden (403) from accessing admin-only employee management")
    void testEmployeeForbiddenFromEmployeeManagement() throws Exception
    {
        Employee employee = new Employee("Staff User", "staff.user@test.com", passwordEncoder.encode("Pass123"), "HR", Role.EMPLOYEE, "123");
        employeeRepository.save(employee);

        String employeeToken = jwtUtil.generateToken(employee);

        mockMvc.perform(get("/api/employees")
                .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("Forbidden")));

        String newEmpPayload = """
                {
                    "name": "New Emp",
                    "email": "newemp@test.com",
                    "password": "Password123",
                    "department": "Finance",
                    "role": "EMPLOYEE"
                }
                """;

        mockMvc.perform(post("/api/employees")
                .header("Authorization", "Bearer " + employeeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(newEmpPayload))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    @Test
    @DisplayName("EMPLOYEE role is forbidden (403) from assigning technicians")
    void testEmployeeForbiddenFromAssigningTechnician() throws Exception
    {
        Employee emp = new Employee("Emp A", "empa@test.com", passwordEncoder.encode("Pass123"), "HR", Role.EMPLOYEE, "123");
        Employee tech = new Employee("Tech A", "techa@test.com", passwordEncoder.encode("Pass123"), "IT", Role.TECHNICIAN, "456");
        employeeRepository.save(emp);
        employeeRepository.save(tech);

        Ticket ticket = ticketService.createTicket(new CreateTicketRequest("Printer paper jam", "Jam in tray 2", "Hardware", TicketPriority.LOW, emp.getId()));

        String employeeToken = jwtUtil.generateToken(emp);

        mockMvc.perform(patch("/api/tickets/" + ticket.getId() + "/assign/" + tech.getId())
                .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    @Test
    @DisplayName("EMPLOYEE role is forbidden (403) from viewing dashboard reports")
    void testEmployeeForbiddenFromDashboard() throws Exception
    {
        Employee emp = new Employee("Emp B", "empb@test.com", passwordEncoder.encode("Pass123"), "Sales", Role.EMPLOYEE, "123");
        employeeRepository.save(emp);

        String employeeToken = jwtUtil.generateToken(emp);

        mockMvc.perform(get("/api/dashboard/summary")
                .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));

        mockMvc.perform(get("/api/tickets/overdue")
                .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    @Test
    @DisplayName("TECHNICIAN role is allowed to view dashboard statistics")
    void testTechnicianAllowedOnDashboard() throws Exception
    {
        Employee tech = new Employee("Tech B", "techb@test.com", passwordEncoder.encode("Pass123"), "IT", Role.TECHNICIAN, "999");
        employeeRepository.save(tech);

        String techToken = jwtUtil.generateToken(tech);

        mockMvc.perform(get("/api/dashboard/summary")
                .header("Authorization", "Bearer " + techToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Identity spoofing prevention: Reject ticket creation with someone else's employeeId")
    void testIdentitySpoofingCreateTicketRejected() throws Exception
    {
        Employee realUser = new Employee("Real User", "real.user@test.com", passwordEncoder.encode("Pass123"), "Marketing", Role.EMPLOYEE, "111");
        Employee victimUser = new Employee("Victim User", "victim.user@test.com", passwordEncoder.encode("Pass123"), "Finance", Role.EMPLOYEE, "222");
        employeeRepository.save(realUser);
        employeeRepository.save(victimUser);

        String token = jwtUtil.generateToken(realUser);

        String spoofedPayload = """
                {
                    "title": "Malicious ticket",
                    "description": "Trying to impersonate victim",
                    "category": "Software",
                    "priority": "HIGH",
                    "employeeId": %d
                }
                """.formatted(victimUser.getId());

        mockMvc.perform(post("/api/tickets")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(spoofedPayload))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.message", is("Identity spoofing rejected: You cannot create tickets on behalf of another employee.")));
    }

    @Test
    @DisplayName("Identity spoofing prevention: Reject comment with someone else's employeeId")
    void testIdentitySpoofingCommentRejected() throws Exception
    {
        Employee owner = new Employee("Ticket Owner", "owner@test.com", passwordEncoder.encode("Pass123"), "IT", Role.EMPLOYEE, "111");
        Employee imposter = new Employee("Imposter", "imposter@test.com", passwordEncoder.encode("Pass123"), "HR", Role.EMPLOYEE, "222");
        employeeRepository.save(owner);
        employeeRepository.save(imposter);

        Ticket ticket = ticketService.createTicket(new CreateTicketRequest("VPN connection issue", "Cannot connect", "Network", TicketPriority.MEDIUM, owner.getId()));

        String ownerToken = jwtUtil.generateToken(owner);

        String spoofedComment = """
                {
                    "employeeId": %d,
                    "commentText": "Fake comment text"
                }
                """.formatted(imposter.getId());

        mockMvc.perform(post("/api/tickets/" + ticket.getId() + "/comments")
                .header("Authorization", "Bearer " + ownerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(spoofedComment))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.message", is("Identity spoofing rejected: You cannot post comments on behalf of another employee.")));
    }

    @Test
    @DisplayName("Ownership check: Regular employee cannot view or comment on another employee's ticket")
    void testEmployeeCannotAccessOtherEmployeesTicket() throws Exception
    {
        Employee userA = new Employee("User A", "usera@test.com", passwordEncoder.encode("Pass123"), "HR", Role.EMPLOYEE, "111");
        Employee userB = new Employee("User B", "userb@test.com", passwordEncoder.encode("Pass123"), "Sales", Role.EMPLOYEE, "222");
        employeeRepository.save(userA);
        employeeRepository.save(userB);

        Ticket ticketA = ticketService.createTicket(new CreateTicketRequest("User A Issue", "Private problem", "Software", TicketPriority.LOW, userA.getId()));

        String userBToken = jwtUtil.generateToken(userB);

        mockMvc.perform(get("/api/tickets/" + ticketA.getId())
                .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));

        String commentPayload = """
                {
                    "employeeId": %d,
                    "commentText": "User B meddling in User A's ticket"
                }
                """.formatted(userB.getId());

        mockMvc.perform(post("/api/tickets/" + ticketA.getId() + "/comments")
                .header("Authorization", "Bearer " + userBToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(commentPayload))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }
}