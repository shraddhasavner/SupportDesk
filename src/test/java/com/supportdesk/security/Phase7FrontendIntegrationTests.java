package com.supportdesk.security;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supportdesk.controller.CreateTicketRequest;
import com.supportdesk.controller.LoginResponse;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class Phase7FrontendIntegrationTests
{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Phase 7.2 - Full Admin Flow: Login -> Get Token -> Fetch Dashboard Summary & Ticket List")
    void testAdminLoginAndDashboardTicketFlow() throws Exception
    {
        Employee admin = new Employee("Admin Leader", "admin.flow@test.com", passwordEncoder.encode("SecretPass123"), "IT", Role.ADMIN, "999");
        employeeRepository.save(admin);

        ticketService.createTicket(new CreateTicketRequest("Server Outage", "Primary web server unresponsive", "Hardware", TicketPriority.CRITICAL, admin.getId()));

        String loginPayload = """
                {
                    "email": "admin.flow@test.com",
                    "password": "SecretPass123"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.role", is("ADMIN")))
                .andExpect(jsonPath("$.name", is("Admin Leader")))
                .andReturn();

        LoginResponse loginResponse = objectMapper.readValue(loginResult.getResponse().getContentAsString(), LoginResponse.class);
        String jwtToken = loginResponse.getToken();

        mockMvc.perform(get("/api/dashboard/summary")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTickets").exists())
                .andExpect(jsonPath("$.inProgressTickets").exists())
                .andExpect(jsonPath("$.overdueTickets").exists())
                .andExpect(jsonPath("$.resolvedTickets").exists());

        mockMvc.perform(get("/api/tickets")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].title", hasItem("Server Outage")))
                .andExpect(jsonPath("$[*].priority", hasItem("CRITICAL")));
    }

    @Test
    @DisplayName("Phase 7.2 - Employee Flow: Can view own tickets, but gets 403 Forbidden on dashboard summary")
    void testEmployeeLoginAndRoleRestrictions() throws Exception
    {
        Employee employeeA = new Employee("Staff Alice", "alice@test.com", passwordEncoder.encode("Pass123"), "Sales", Role.EMPLOYEE, "111");
        Employee employeeB = new Employee("Staff Bob", "bob@test.com", passwordEncoder.encode("Pass123"), "Marketing", Role.EMPLOYEE, "222");
        employeeRepository.save(employeeA);
        employeeRepository.save(employeeB);

        ticketService.createTicket(new CreateTicketRequest("Alice Laptop", "Screen broken", "Hardware", TicketPriority.LOW, employeeA.getId()));
        ticketService.createTicket(new CreateTicketRequest("Bob Printer", "Paper jam", "Hardware", TicketPriority.LOW, employeeB.getId()));

        String loginPayload = """
                {
                    "email": "alice@test.com",
                    "password": "Pass123"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse loginResponse = objectMapper.readValue(loginResult.getResponse().getContentAsString(), LoginResponse.class);
        String aliceToken = loginResponse.getToken();

        mockMvc.perform(get("/api/dashboard/summary")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));

        mockMvc.perform(get("/api/tickets")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Alice Laptop")));
    }

    @Test
    @DisplayName("Phase 7.3 - Ticket Creation, Details, Comments, and Activity History End-to-End Flow")
    void testPhase73CreationDetailsCommentsAndActivity() throws Exception
    {
        Employee staff = new Employee("Deepak Verma", "deepak@test.com", passwordEncoder.encode("Pass1234"), "Finance", Role.EMPLOYEE, "333");
        employeeRepository.save(staff);

        String loginPayload = """
                {
                    "email": "deepak@test.com",
                    "password": "Pass1234"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse loginResponse = objectMapper.readValue(loginResult.getResponse().getContentAsString(), LoginResponse.class);
        String token = loginResponse.getToken();

        String createPayload = """
                {
                    "title": "Dual Monitor setup issue",
                    "description": "Second display is flickering intermittently after graphics driver update",
                    "category": "Hardware",
                    "priority": "HIGH",
                    "employeeId": %d
                }
                """.formatted(staff.getId());

        MvcResult createResult = mockMvc.perform(post("/api/tickets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Dual Monitor setup issue")))
                .andExpect(jsonPath("$.category", is("Hardware")))
                .andExpect(jsonPath("$.priority", is("HIGH")))
                .andExpect(jsonPath("$.status", is("OPEN")))
                .andExpect(jsonPath("$.ticketNumber").exists())
                .andExpect(jsonPath("$.slaDeadline").exists())
                .andReturn();

        Number ticketIdNum = com.jayway.jsonpath.JsonPath.read(createResult.getResponse().getContentAsString(), "$.id");
        Long ticketId = ticketIdNum.longValue();

        mockMvc.perform(get("/api/tickets/" + ticketId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(ticketId.intValue())))
                .andExpect(jsonPath("$.title", is("Dual Monitor setup issue")))
                .andExpect(jsonPath("$.employee.name", is("Deepak Verma")));

        String commentPayload = """
                {
                    "employeeId": %d,
                    "commentText": "I tried reseating the HDMI cable but flickering persists."
                }
                """.formatted(staff.getId());

        mockMvc.perform(post("/api/tickets/" + ticketId + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.commentText", is("I tried reseating the HDMI cable but flickering persists.")))
                .andExpect(jsonPath("$.author.name", is("Deepak Verma")));

        mockMvc.perform(get("/api/tickets/" + ticketId + "/comments")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].commentText", is("I tried reseating the HDMI cable but flickering persists.")));

        mockMvc.perform(get("/api/tickets/" + ticketId + "/activities")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].activityType", is("TICKET_CREATED")))
                .andExpect(jsonPath("$[1].activityType", is("COMMENT_ADDED")));
    }

    @Test
    @DisplayName("Phase 7.3 - Ownership & Spoofing Protection: Other employee cannot access or comment on ticket")
    void testPhase73OwnershipProtection() throws Exception
    {
        Employee owner = new Employee("Owner User", "owner.user@test.com", passwordEncoder.encode("Pass123"), "HR", Role.EMPLOYEE, "888");
        Employee other = new Employee("Other User", "other.user@test.com", passwordEncoder.encode("Pass123"), "Marketing", Role.EMPLOYEE, "999");
        employeeRepository.save(owner);
        employeeRepository.save(other);

        Ticket ownerTicket = ticketService.createTicket(new CreateTicketRequest("Confidential HR ticket", "Private details", "Access & Security", TicketPriority.MEDIUM, owner.getId()));

        String loginPayload = """
                {
                    "email": "other.user@test.com",
                    "password": "Pass123"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn();

        String otherToken = objectMapper.readValue(loginResult.getResponse().getContentAsString(), LoginResponse.class).getToken();

        mockMvc.perform(get("/api/tickets/" + ownerTicket.getId())
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));

        String commentPayload = """
                {
                    "employeeId": %d,
                    "commentText": "Attempting unauthorized comment"
                }
                """.formatted(other.getId());

        mockMvc.perform(post("/api/tickets/" + ownerTicket.getId() + "/comments")
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentPayload))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    @Test
    @DisplayName("Phase 7.2 - Login Failures: Invalid password & deactivated account return 401 with friendly messages")
    void testLoginFailureFriendlyMessages() throws Exception
    {
        Employee activeUser = new Employee("Active Person", "active@test.com", passwordEncoder.encode("GoodPassword"), "IT", Role.TECHNICIAN, "555");
        Employee inactiveUser = new Employee("Inactive Person", "inactive@test.com", passwordEncoder.encode("GoodPassword"), "IT", Role.TECHNICIAN, "666");
        inactiveUser.setActive(false);
        employeeRepository.save(activeUser);
        employeeRepository.save(inactiveUser);

        String wrongPassPayload = """
                {
                    "email": "active@test.com",
                    "password": "WrongPassword"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(wrongPassPayload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.message", is("Invalid email or password.")));

        String inactivePayload = """
                {
                    "email": "inactive@test.com",
                    "password": "GoodPassword"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inactivePayload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.message", is("Account is deactivated. Please contact an administrator.")));
    }

    @Test
    @DisplayName("Phase 7.3, 7.4 & 7.5 - Frontend SPA Markup: index.html has Login View, Dashboard View, Modals, Staff Panel, and Employee Directory")
    void testFrontendMarkupStructure() throws Exception
    {
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("loginView")))
                .andExpect(content().string(containsString("dashboardView")))
                .andExpect(content().string(containsString("dashboardSectionView")))
                .andExpect(content().string(containsString("employeesSectionView")))
                .andExpect(content().string(containsString("adminNavSectionTitle")))
                .andExpect(content().string(containsString("sidebarEmployeesBtn")))
                .andExpect(content().string(containsString("employeesTableBody")))
                .andExpect(content().string(containsString("openCreateEmployeeBtn")))
                .andExpect(content().string(containsString("employeeModal")))
                .andExpect(content().string(containsString("employeeForm")))
                .andExpect(content().string(containsString("employeeSearchInput")))
                .andExpect(content().string(containsString("employeeRoleFilter")))
                .andExpect(content().string(containsString("employeeDeptFilter")))
                .andExpect(content().string(containsString("createTicketModal")))
                .andExpect(content().string(containsString("ticketDetailModal")))
                .andExpect(content().string(containsString("detailStaffActionsPanel")))
                .andExpect(content().string(containsString("assignTechnicianSection")))
                .andExpect(content().string(containsString("updateStatusSection")))
                .andExpect(content().string(containsString("resolveTicketSection")))
                .andExpect(content().string(containsString("addCommentForm")))
                .andExpect(content().string(containsString("tabActivitiesContent")))
                .andExpect(content().string(containsString("js/api.js")))
                .andExpect(content().string(containsString("statTotalTickets")))
                .andExpect(content().string(containsString("ticketsTableBody")));
    }

    @Test
    @DisplayName("Phase 7.4 - Admin Technician Assignment, Status Transition, and Ticket Resolution Flow")
    void testPhase74AssignmentStatusAndResolutionFlow() throws Exception
    {
        Employee admin = new Employee("Admin Boss", "admin.p74@test.com", passwordEncoder.encode("Pass123"), "IT", Role.ADMIN, "111");
        Employee tech = new Employee("Tech Ravi", "tech.ravi@test.com", passwordEncoder.encode("Pass123"), "IT", Role.TECHNICIAN, "222");
        Employee requester = new Employee("Requester Tina", "tina@test.com", passwordEncoder.encode("Pass123"), "HR", Role.EMPLOYEE, "333");
        employeeRepository.save(admin);
        employeeRepository.save(tech);
        employeeRepository.save(requester);

        Ticket ticket = ticketService.createTicket(new CreateTicketRequest("Laptop Battery Failure", "Battery does not charge beyond 5%", "Hardware", TicketPriority.HIGH, requester.getId()));

        String adminLoginPayload = """
                {
                    "email": "admin.p74@test.com",
                    "password": "Pass123"
                }
                """;

        MvcResult adminLoginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adminLoginPayload))
                .andExpect(status().isOk())
                .andReturn();

        String adminToken = objectMapper.readValue(adminLoginResult.getResponse().getContentAsString(), LoginResponse.class).getToken();

        mockMvc.perform(get("/api/employees/technicians")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].role", is("TECHNICIAN")));

        mockMvc.perform(patch("/api/tickets/" + ticket.getId() + "/assign/" + tech.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedTechnician.id", is(tech.getId().intValue())))
                .andExpect(jsonPath("$.status", is("ASSIGNED")));

        mockMvc.perform(patch("/api/tickets/" + ticket.getId() + "/status?status=IN_PROGRESS")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));

        String resolvePayload = """
                {
                    "resolutionNotes": "Replaced faulty battery cell and calibrated power adapter.",
                    "markResolved": true
                }
                """;

        mockMvc.perform(patch("/api/tickets/" + ticket.getId() + "/resolve")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resolvePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RESOLVED")))
                .andExpect(jsonPath("$.resolutionNotes", is("Replaced faulty battery cell and calibrated power adapter.")));

        mockMvc.perform(get("/api/tickets/" + ticket.getId() + "/activities")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].activityType", is("TICKET_CREATED")))
                .andExpect(jsonPath("$[1].activityType", is("TECHNICIAN_ASSIGNED")))
                .andExpect(jsonPath("$[2].activityType", is("STATUS_CHANGED")))
                .andExpect(jsonPath("$[3].activityType", is("TICKET_RESOLVED")));
    }

    @Test
    @DisplayName("Phase 7.4 - Role Restrictions: Employee cannot assign technicians, change status, or resolve tickets")
    void testPhase74RoleRestrictionsForEmployee() throws Exception
    {
        Employee tech = new Employee("Tech Kumar", "tech.kumar@test.com", passwordEncoder.encode("Pass123"), "IT", Role.TECHNICIAN, "444");
        Employee employee = new Employee("Staff Pooja", "pooja@test.com", passwordEncoder.encode("Pass123"), "Finance", Role.EMPLOYEE, "555");
        employeeRepository.save(tech);
        employeeRepository.save(employee);

        Ticket ticket = ticketService.createTicket(new CreateTicketRequest("Printer issue", "No toner", "Hardware", TicketPriority.LOW, employee.getId()));

        String employeeLogin = """
                {
                    "email": "pooja@test.com",
                    "password": "Pass123"
                }
                """;

        MvcResult empResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeLogin))
                .andExpect(status().isOk())
                .andReturn();

        String employeeToken = objectMapper.readValue(empResult.getResponse().getContentAsString(), LoginResponse.class).getToken();

        mockMvc.perform(get("/api/employees/technicians")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));

        mockMvc.perform(patch("/api/tickets/" + ticket.getId() + "/assign/" + tech.getId())
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));

        mockMvc.perform(patch("/api/tickets/" + ticket.getId() + "/status?status=IN_PROGRESS")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));

        String resolvePayload = """
                {
                    "resolutionNotes": "Employee trying to resolve",
                    "markResolved": true
                }
                """;

        mockMvc.perform(patch("/api/tickets/" + ticket.getId() + "/resolve")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resolvePayload))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    @Test
    @DisplayName("Phase 7.5 - Admin Employee Administration Flow: List, Create, Update, Deactivate, Reactivate")
    void testPhase75AdminEmployeeCrudFlow() throws Exception
    {
        Employee admin = new Employee("Admin Super", "admin.p75@test.com", passwordEncoder.encode("Pass123"), "IT", Role.ADMIN, "101");
        employeeRepository.save(admin);

        String adminLoginPayload = """
                {
                    "email": "admin.p75@test.com",
                    "password": "Pass123"
                }
                """;

        MvcResult adminLoginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adminLoginPayload))
                .andExpect(status().isOk())
                .andReturn();

        String adminToken = objectMapper.readValue(adminLoginResult.getResponse().getContentAsString(), LoginResponse.class).getToken();

        mockMvc.perform(get("/api/employees")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        String newEmpPayload = """
                {
                    "name": "Sunita Rao",
                    "email": "sunita.rao@supportdesk.com",
                    "password": "Password123",
                    "department": "Engineering",
                    "role": "TECHNICIAN",
                    "phone": "9876500001"
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/employees")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newEmpPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name", is("Sunita Rao")))
                .andExpect(jsonPath("$.email", is("sunita.rao@supportdesk.com")))
                .andExpect(jsonPath("$.department", is("Engineering")))
                .andExpect(jsonPath("$.role", is("TECHNICIAN")))
                .andExpect(jsonPath("$.phone", is("9876500001")))
                .andExpect(jsonPath("$.active", is(true)))
                .andExpect(jsonPath("$", not(hasKey("password"))))
                .andReturn();

        Number empIdNum = com.jayway.jsonpath.JsonPath.read(createResult.getResponse().getContentAsString(), "$.id");
        Long createdEmpId = empIdNum.longValue();

        String updatePayload = """
                {
                    "name": "Sunita Rao Sharma",
                    "email": "sunita.sharma@supportdesk.com",
                    "password": "NewSecretPass456",
                    "department": "Infrastructure",
                    "role": "TECHNICIAN",
                    "phone": "9876500099"
                }
                """;

        mockMvc.perform(put("/api/employees/" + createdEmpId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Sunita Rao Sharma")))
                .andExpect(jsonPath("$.email", is("sunita.sharma@supportdesk.com")))
                .andExpect(jsonPath("$.department", is("Infrastructure")))
                .andExpect(jsonPath("$", not(hasKey("password"))));

        mockMvc.perform(patch("/api/employees/" + createdEmpId + "/deactivate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active", is(false)));

        mockMvc.perform(patch("/api/employees/" + createdEmpId + "/activate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    @DisplayName("Phase 7.5 - Role Restrictions: Employee cannot access employee directory, create, or deactivate staff")
    void testPhase75RoleRestrictionsForEmployee() throws Exception
    {
        Employee regular = new Employee("Regular Joe", "joe.emp@test.com", passwordEncoder.encode("Pass123"), "Operations", Role.EMPLOYEE, "102");
        Employee target = new Employee("Target Staff", "target.staff@test.com", passwordEncoder.encode("Pass123"), "HR", Role.EMPLOYEE, "103");
        employeeRepository.save(regular);
        employeeRepository.save(target);

        String loginPayload = """
                {
                    "email": "joe.emp@test.com",
                    "password": "Pass123"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn();

        String empToken = objectMapper.readValue(loginResult.getResponse().getContentAsString(), LoginResponse.class).getToken();

        mockMvc.perform(get("/api/employees")
                        .header("Authorization", "Bearer " + empToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));

        String createPayload = """
                {
                    "name": "Hacker",
                    "email": "hacker@test.com",
                    "password": "Password123",
                    "department": "IT",
                    "role": "ADMIN"
                }
                """;

        mockMvc.perform(post("/api/employees")
                        .header("Authorization", "Bearer " + empToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));

        mockMvc.perform(patch("/api/employees/" + target.getId() + "/deactivate")
                        .header("Authorization", "Bearer " + empToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));

        mockMvc.perform(patch("/api/employees/" + target.getId() + "/activate")
                        .header("Authorization", "Bearer " + empToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    @Test
    @DisplayName("Phase 7.6 - In-App Notification UI Elements and Endpoints Present")
    void testPhase76NotificationUiElementsAndEndpoints() throws Exception
    {
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("notification-container")))
                .andExpect(content().string(containsString("notificationBellBtn")))
                .andExpect(content().string(containsString("notificationBadge")))
                .andExpect(content().string(containsString("notificationDropdown")))
                .andExpect(content().string(containsString("markAllReadBtn")));

        mockMvc.perform(get("/css/styles.css"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(".notification-bell-btn")))
                .andExpect(content().string(containsString(".notification-badge")))
                .andExpect(content().string(containsString(".notification-dropdown")))
                .andExpect(content().string(containsString(".notification-item.unread")));

        mockMvc.perform(get("/js/app.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("loadUnreadCount")))
                .andExpect(content().string(containsString("loadNotifications")))
                .andExpect(content().string(containsString("markNotificationAsRead")))
                .andExpect(content().string(containsString("markAllNotificationsAsRead")));
    }
}