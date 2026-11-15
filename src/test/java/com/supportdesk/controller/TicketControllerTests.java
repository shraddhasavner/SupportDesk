package com.supportdesk.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.supportdesk.entity.Employee;
import com.supportdesk.entity.Role;
import com.supportdesk.entity.Ticket;
import com.supportdesk.entity.TicketPriority;
import com.supportdesk.repository.EmployeeRepository;
import com.supportdesk.repository.TicketRepository;
import com.supportdesk.security.JwtUtil;
import com.supportdesk.service.TicketService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TicketControllerTests
{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private JwtUtil jwtUtil;

    private String adminToken;

    @BeforeEach
    void setUp()
    {
        Employee admin = new Employee("Admin Main", "admin.ticket.test@supportdesk.com", "password123", "IT", Role.ADMIN, "000");
        employeeRepository.save(admin);
        adminToken = jwtUtil.generateToken(admin);
    }

    @Test
    @DisplayName("POST /api/tickets - Creates ticket and returns 201 Created")
    void testCreateTicketApi() throws Exception
    {
        Employee creator = new Employee("Kavita Rao", "kavita@test.com", "password123", "Finance", Role.EMPLOYEE, "8881112223");
        employeeRepository.save(creator);

        String payload = """
                {
                    "title": "Cannot open Excel macro file",
                    "description": "Macro throws Visual Basic 1004 runtime error.",
                    "category": "Software",
                    "priority": "HIGH",
                    "employeeId": %d
                }
                """.formatted(creator.getId());

        mockMvc.perform(post("/api/tickets")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.ticketNumber", startsWith("TCK-")))
                .andExpect(jsonPath("$.status", is("OPEN")))
                .andExpect(jsonPath("$.priority", is("HIGH")))
                .andExpect(jsonPath("$.title", is("Cannot open Excel macro file")))
                .andExpect(jsonPath("$.employee.name", is("Kavita Rao")))
                // Ensure employee password is never exposed in the nested employee object
                .andExpect(jsonPath("$.employee", not(hasKey("password"))));
    }

    @Test
    @DisplayName("POST /api/tickets - Returns 400 Bad Request on validation failure")
    void testCreateTicketValidationFailure() throws Exception
    {
        String invalidPayload = """
                {
                    "title": "",
                    "description": "",
                    "category": "",
                    "employeeId": null
                }
                """;

        mockMvc.perform(post("/api/tickets")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.validationErrors.title").exists())
                .andExpect(jsonPath("$.validationErrors.description").exists())
                .andExpect(jsonPath("$.validationErrors.category").exists());
    }

    @Test
    @DisplayName("GET /api/tickets - Lists tickets with filtering by status and priority")
    void testGetAllTicketsWithFilters() throws Exception
    {
        Employee creator = new Employee("Gaurav Jain", "gaurav@test.com", "password123", "IT", Role.EMPLOYEE, "112233");
        employeeRepository.save(creator);

        ticketService.createTicket(new CreateTicketRequest("Issue 1", "Desc 1", "Software", TicketPriority.LOW, creator.getId()));
        ticketService.createTicket(new CreateTicketRequest("Issue 2", "Desc 2", "Hardware", TicketPriority.CRITICAL, creator.getId()));

        mockMvc.perform(get("/api/tickets")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/tickets?priority=CRITICAL")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].priority", is("CRITICAL")));
    }

    @Test
    @DisplayName("GET /api/tickets/{id} - Returns ticket by ID or 404 when not found")
    void testGetTicketById() throws Exception
    {
        Employee creator = new Employee("Meena Das", "meena@test.com", "password123", "Operations", Role.EMPLOYEE, "445566");
        employeeRepository.save(creator);

        Ticket ticket = ticketService.createTicket(new CreateTicketRequest("WiFi Drop", "WiFi disconnects", "Network", TicketPriority.MEDIUM, creator.getId()));

        mockMvc.perform(get("/api/tickets/" + ticket.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("WiFi Drop")));

        mockMvc.perform(get("/api/tickets/999999")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")));
    }

    @Test
    @DisplayName("PATCH /api/tickets/{id}/assign/{technicianId} - Assigns technician or rejects non-technician")
    void testAssignTechnicianEndpoints() throws Exception
    {
        Employee creator = new Employee("Staff User", "staff@test.com", "password123", "HR", Role.EMPLOYEE, "1001");
        Employee tech = new Employee("IT Pro Tech", "techpro@test.com", "password123", "IT", Role.TECHNICIAN, "1002");
        Employee nonTech = new Employee("HR Manager", "hrmgr@test.com", "password123", "HR", Role.EMPLOYEE, "1003");
        employeeRepository.save(creator);
        employeeRepository.save(tech);
        employeeRepository.save(nonTech);

        Ticket ticket = ticketService.createTicket(new CreateTicketRequest("OS Corrupted", "Reinstall needed", "Software", TicketPriority.HIGH, creator.getId()));

        // Assign a valid technician
        mockMvc.perform(patch("/api/tickets/" + ticket.getId() + "/assign/" + tech.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ASSIGNED")))
                .andExpect(jsonPath("$.assignedTechnician.name", is("IT Pro Tech")));

        // Assigning an employee instead of a technician should fail
        mockMvc.perform(patch("/api/tickets/" + ticket.getId() + "/assign/" + nonTech.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("does not have the TECHNICIAN role")));
    }

    @Test
    @DisplayName("PATCH /api/tickets/{id}/assign/{technicianId} - Records authenticated admin as activity actor while keeping technician as assignment target")
    void testAssignTechnicianPatchRecordsAdminActorInActivityHistory() throws Exception
    {
        Employee creator = new Employee("Employee Creator", "creator.patch@test.com", "password123", "HR", Role.EMPLOYEE, "1001");
        Employee tech = new Employee("Assigned Tech", "assigned.tech.patch@test.com", "password123", "IT", Role.TECHNICIAN, "1002");
        employeeRepository.save(creator);
        employeeRepository.save(tech);

        Ticket ticket = ticketService.createTicket(new CreateTicketRequest("Laptop black screen", "Display not powering on", "Hardware", TicketPriority.HIGH, creator.getId()));

        // Admin assigns the ticket through the PATCH endpoint
        mockMvc.perform(patch("/api/tickets/" + ticket.getId() + "/assign/" + tech.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ASSIGNED")))
                .andExpect(jsonPath("$.assignedTechnician.id", is(tech.getId().intValue())))
                .andExpect(jsonPath("$.assignedTechnician.name", is("Assigned Tech")));

        // The activity actor must be the authenticated admin, not the assigned technician
        mockMvc.perform(get("/api/tickets/" + ticket.getId() + "/activities")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[1].activityType", is("TECHNICIAN_ASSIGNED")))
                .andExpect(jsonPath("$[1].performedBy.name", is("Admin Main")))
                .andExpect(jsonPath("$[1].performedBy.email", is("admin.ticket.test@supportdesk.com")))
                .andExpect(jsonPath("$[1].performedBy.role", is("ADMIN")))
                .andExpect(jsonPath("$[1].description", is("Ticket assigned to technician Assigned Tech")));
    }

    @Test
    @DisplayName("PUT /api/tickets/{id}/assign - Query param endpoint records admin actor in activity history")
    void testAssignTechnicianPutParamRecordsAdminActorInActivityHistory() throws Exception
    {
        Employee creator = new Employee("Employee Creator 2", "creator.put@test.com", "password123", "Operations", Role.EMPLOYEE, "1003");
        Employee tech = new Employee("Network Specialist", "net.tech.put@test.com", "password123", "IT", Role.TECHNICIAN, "1004");
        employeeRepository.save(creator);
        employeeRepository.save(tech);

        Ticket ticket = ticketService.createTicket(new CreateTicketRequest("VPN tunnel failure", "Gateway unreachable", "Network", TicketPriority.CRITICAL, creator.getId()));

        // Admin assigns the ticket using the PUT query parameter endpoint
        mockMvc.perform(put("/api/tickets/" + ticket.getId() + "/assign?technicianId=" + tech.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ASSIGNED")))
                .andExpect(jsonPath("$.assignedTechnician.name", is("Network Specialist")));

        // Confirm that the admin is recorded as the activity actor
        mockMvc.perform(get("/api/tickets/" + ticket.getId() + "/activities")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[1].activityType", is("TECHNICIAN_ASSIGNED")))
                .andExpect(jsonPath("$[1].performedBy.name", is("Admin Main")))
                .andExpect(jsonPath("$[1].performedBy.email", is("admin.ticket.test@supportdesk.com")))
                .andExpect(jsonPath("$[1].performedBy.role", is("ADMIN")))
                .andExpect(jsonPath("$[1].description", is("Ticket assigned to technician Network Specialist")));
    }

    @Test
    @DisplayName("PATCH /api/tickets/{id}/status - Updates ticket status")
    void testUpdateTicketStatus() throws Exception
    {
        Employee creator = new Employee("Vikram Patel", "vikram@test.com", "password123", "Legal", Role.EMPLOYEE, "2001");
        employeeRepository.save(creator);

        Ticket ticket = ticketService.createTicket(new CreateTicketRequest("Outlook sync issue", "Emails not arriving", "Software", TicketPriority.MEDIUM, creator.getId()));

        mockMvc.perform(patch("/api/tickets/" + ticket.getId() + "/status?status=IN_PROGRESS")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));
    }

    @Test
    @DisplayName("PATCH /api/tickets/{id}/resolve - Adds resolution notes and marks RESOLVED")
    void testResolveTicket() throws Exception
    {
        Employee creator = new Employee("Anita Sen", "anita@test.com", "password123", "Audit", Role.EMPLOYEE, "3001");
        employeeRepository.save(creator);

        Ticket ticket = ticketService.createTicket(new CreateTicketRequest("Mouse wheel broken", "Cannot scroll", "Hardware", TicketPriority.LOW, creator.getId()));

        String resolvePayload = """
                {
                    "resolutionNotes": "Provided new optical mouse from IT inventory.",
                    "markResolved": true
                }
                """;

        mockMvc.perform(patch("/api/tickets/" + ticket.getId() + "/resolve")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(resolvePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RESOLVED")))
                .andExpect(jsonPath("$.resolutionNotes", is("Provided new optical mouse from IT inventory.")));
    }

    @Test
    @DisplayName("GET /api/tickets/overdue - Returns overdue tickets")
    void testGetOverdueTicketsApi() throws Exception
    {
        Employee creator = new Employee("Rajesh Gupta", "rajesh.gupta@test.com", "password123", "IT", Role.EMPLOYEE, "5001");
        employeeRepository.save(creator);

        Ticket ticket = ticketService.createTicket(new CreateTicketRequest("Printer offline", "Offline in hall", "Hardware", TicketPriority.HIGH, creator.getId()));

        // Set an expired deadline to test overdue ticket handling
        ticket.setSlaDeadline(LocalDateTime.now().minusHours(3));
        ticketRepository.save(ticket);

        mockMvc.perform(get("/api/tickets/overdue")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].overdue", is(true)))
                .andExpect(jsonPath("$[0].slaDeadline").exists());
    }

    @Test
    @DisplayName("GET /api/tickets/{id}/sla - Returns SLA deadline and metrics")
    void testGetTicketSlaStatusApi() throws Exception
    {
        Employee creator = new Employee("Pooja Nair", "pooja.nair@test.com", "password123", "HR", Role.EMPLOYEE, "6001");
        employeeRepository.save(creator);

        Ticket ticket = ticketService.createTicket(new CreateTicketRequest("VPN credentials expired", "Password reset required", "Access", TicketPriority.CRITICAL, creator.getId()));

        mockMvc.perform(get("/api/tickets/" + ticket.getId() + "/sla")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketId", is(ticket.getId().intValue())))
                .andExpect(jsonPath("$.priority", is("CRITICAL")))
                .andExpect(jsonPath("$.totalSlaHours", is(4)))
                .andExpect(jsonPath("$.overdue", is(false)))
                .andExpect(jsonPath("$.slaDeadline").exists())
                .andExpect(jsonPath("$.minutesRemaining").exists());
    }
}