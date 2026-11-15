package com.supportdesk.controller;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class TicketCommentAndActivityControllerTests
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
        Employee admin = new Employee("Admin Main", "admin.comments.test@supportdesk.com", "password123", "IT", Role.ADMIN, "000");
        employeeRepository.save(admin);
        adminToken = jwtUtil.generateToken(admin);
    }

    @Test
    @DisplayName("POST /api/tickets/{ticketId}/comments - Successfully creates comment and returns 201 Created")
    void testAddCommentApiSuccess() throws Exception
    {
        Employee creator = new Employee("Indira Sen", "indira@test.com", "password123", "Finance", Role.EMPLOYEE, "101010");
        Employee tech = new Employee("Jitendra IT", "jitendra@test.com", "password123", "IT", Role.TECHNICIAN, "202020");
        employeeRepository.save(creator);
        employeeRepository.save(tech);

        Ticket ticket = ticketService.createTicket(new CreateTicketRequest("SAP logon failure", "SAP GUI connection timeout", "Software", TicketPriority.HIGH, creator.getId()));

        String payload = """
                {
                    "employeeId": %d,
                    "commentText": "Checked user authorization in SAP BASIS. Reinstalling SAP GUI patch 5."
                }
                """.formatted(tech.getId());

        mockMvc.perform(post("/api/tickets/" + ticket.getId() + "/comments")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.commentText", is("Checked user authorization in SAP BASIS. Reinstalling SAP GUI patch 5.")))
                .andExpect(jsonPath("$.author.id", is(tech.getId().intValue())))
                .andExpect(jsonPath("$.author.name", is("Jitendra IT")))
                // Ensure employee password is never exposed in the response
                .andExpect(jsonPath("$.author", not(hasKey("password"))))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @DisplayName("POST /api/tickets/{ticketId}/comments - Rejects blank comment and missing employee ID with 400 Bad Request")
    void testAddCommentValidationFailure() throws Exception
    {
        Employee creator = new Employee("Kiran Bala", "kiran@test.com", "password123", "HR", Role.EMPLOYEE, "303030");
        employeeRepository.save(creator);

        Ticket ticket = ticketService.createTicket(new CreateTicketRequest("Headphones buzzing", "Audio distortion", "Hardware", TicketPriority.LOW, creator.getId()));

        String invalidPayload = """
                {
                    "employeeId": null,
                    "commentText": ""
                }
                """;

        mockMvc.perform(post("/api/tickets/" + ticket.getId() + "/comments")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.validationErrors.commentText").exists());
    }

    @Test
    @DisplayName("POST /api/tickets/{ticketId}/comments - Returns 404 when ticket or employee not found")
    void testAddCommentNotFound() throws Exception
    {
        Employee creator = new Employee("Lalit Mohan", "lalit@test.com", "password123", "Sales", Role.EMPLOYEE, "404040");
        employeeRepository.save(creator);

        Ticket ticket = ticketService.createTicket(new CreateTicketRequest("CRM sync fail", "Data not uploading", "Software", TicketPriority.MEDIUM, creator.getId()));

        // Case 1: Ticket not found
        String validPayload = """
                {
                    "employeeId": %d,
                    "commentText": "Valid comment"
                }
                """.formatted(creator.getId());

        mockMvc.perform(post("/api/tickets/999999/comments")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")));

        // Case 2: Employee not found
        String missingEmployeePayload = """
                {
                    "employeeId": 888888,
                    "commentText": "Comment by ghost author"
                }
                """;

        mockMvc.perform(post("/api/tickets/" + ticket.getId() + "/comments")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(missingEmployeePayload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")));
    }

    @Test
    @DisplayName("GET /api/tickets/{ticketId}/comments - Returns list of comments ordered oldest first")
    void testGetCommentsApi() throws Exception
    {
        Employee creator = new Employee("Manish Pandey", "manish@test.com", "password123", "Marketing", Role.EMPLOYEE, "505050");
        employeeRepository.save(creator);

        Ticket ticket = ticketService.createTicket(new CreateTicketRequest("Projector issue", "Lamp burnt", "Hardware", TicketPriority.MEDIUM, creator.getId()));

        ticketService.addComment(ticket.getId(), creator.getId(), "First comment from employee");
        ticketService.addComment(ticket.getId(), creator.getId(), "Second follow-up comment");

        mockMvc.perform(get("/api/tickets/" + ticket.getId() + "/comments")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].commentText", is("First comment from employee")))
                .andExpect(jsonPath("$[1].commentText", is("Second follow-up comment")))
                // Ensure author password is not exposed
                .andExpect(jsonPath("$[0].author", not(hasKey("password"))));
    }

    @Test
    @DisplayName("GET /api/tickets/{ticketId}/comments - Returns 404 when ticket not found")
    void testGetCommentsNotFound() throws Exception
    {
        mockMvc.perform(get("/api/tickets/999999/comments")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("GET /api/tickets/{ticketId}/activities - Returns activity history ordered chronologically")
    void testGetActivitiesApi() throws Exception
    {
        Employee creator = new Employee("Nandini Roy", "nandini@test.com", "password123", "HR", Role.EMPLOYEE, "606060");
        Employee tech = new Employee("Omkar Tech", "omkar@test.com", "password123", "IT", Role.TECHNICIAN, "707070");
        employeeRepository.save(creator);
        employeeRepository.save(tech);

        Ticket ticket = ticketService.createTicket(new CreateTicketRequest("Biometric scanner error", "Fingerprint not recognized", "Hardware", TicketPriority.HIGH, creator.getId()));

        ticketService.assignTechnician(ticket.getId(), tech.getId());
        ticketService.addComment(ticket.getId(), tech.getId(), "Cleaned optical sensor and updated driver.");

        mockMvc.perform(get("/api/tickets/" + ticket.getId() + "/activities")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].activityType", is("TICKET_CREATED")))
                .andExpect(jsonPath("$[1].activityType", is("TECHNICIAN_ASSIGNED")))
                .andExpect(jsonPath("$[2].activityType", is("COMMENT_ADDED")))
                .andExpect(jsonPath("$[0].createdAt").exists())
                .andExpect(jsonPath("$[1].performedBy.name", is("Omkar Tech")))
                .andExpect(jsonPath("$[1].performedBy", not(hasKey("password"))));
    }

    @Test
    @DisplayName("GET /api/tickets/{ticketId}/history - Alias endpoint returns activity history identically")
    void testGetActivitiesAliasHistoryApi() throws Exception
    {
        Employee creator = new Employee("Pankaj Shaw", "pankaj@test.com", "password123", "Finance", Role.EMPLOYEE, "808080");
        employeeRepository.save(creator);

        Ticket ticket = ticketService.createTicket(new CreateTicketRequest("Excel formula freeze", "Calculation stuck at 100%", "Software", TicketPriority.LOW, creator.getId()));

        mockMvc.perform(get("/api/tickets/" + ticket.getId() + "/history")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].activityType", is("TICKET_CREATED")));
    }

    @Test
    @DisplayName("GET /api/tickets/{ticketId}/activities - Returns 404 when ticket not found")
    void testGetActivitiesNotFound() throws Exception
    {
        mockMvc.perform(get("/api/tickets/999999/activities")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }
}