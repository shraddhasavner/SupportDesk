package com.supportdesk.controller;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supportdesk.entity.Employee;
import com.supportdesk.entity.Notification;
import com.supportdesk.entity.NotificationType;
import com.supportdesk.entity.Role;
import com.supportdesk.entity.Ticket;
import com.supportdesk.entity.TicketPriority;
import com.supportdesk.entity.TicketStatus;
import com.supportdesk.repository.EmployeeRepository;
import com.supportdesk.repository.NotificationRepository;
import com.supportdesk.repository.TicketRepository;
import com.supportdesk.security.JwtUtil;
import com.supportdesk.service.NotificationService;
import com.supportdesk.service.TicketService;
import java.util.List;
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
class NotificationIntegrationTests
{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private Employee admin;
    private Employee technician;
    private Employee employee;

    private String adminToken;
    private String techToken;
    private String employeeToken;

    @BeforeEach
    void setUp()
    {
        admin = new Employee("Admin User", "admin.notif@supportdesk.com", "password123", "IT", Role.ADMIN, "101");
        technician = new Employee("Tech User", "tech.notif@supportdesk.com", "password123", "IT", Role.TECHNICIAN, "102");
        employee = new Employee("Staff User", "staff.notif@supportdesk.com", "password123", "Sales", Role.EMPLOYEE, "103");

        employeeRepository.save(admin);
        employeeRepository.save(technician);
        employeeRepository.save(employee);

        adminToken = jwtUtil.generateToken(admin);
        techToken = jwtUtil.generateToken(technician);
        employeeToken = jwtUtil.generateToken(employee);
    }

    @Test
    @DisplayName("Technician Assignment should notify technician and ticket creator, but not the assigning admin")
    void testTechnicianAssignmentNotifications() throws Exception
    {
        // Create a ticket by employee
        CreateTicketRequest req = new CreateTicketRequest("VPN Failure", "Cannot connect to VPN", "Network", TicketPriority.HIGH, employee.getId());
        Ticket ticket = ticketService.createTicket(req);

        // Admin assigns technician via API
        mockMvc.perform(patch("/api/tickets/" + ticket.getId() + "/assign/" + technician.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Verify technician received notification
        List<Notification> techNotifs = notificationRepository.findByRecipientIdOrderByCreatedAtDescIdDesc(technician.getId());
        assertEquals(1, techNotifs.size());
        assertEquals(NotificationType.TECHNICIAN_ASSIGNED, techNotifs.get(0).getType());
        assertTrue(techNotifs.get(0).getMessage().contains("assigned to ticket"));
        assertFalse(techNotifs.get(0).isRead());

        // Verify ticket creator received notification
        List<Notification> empNotifs = notificationRepository.findByRecipientIdOrderByCreatedAtDescIdDesc(employee.getId());
        assertEquals(1, empNotifs.size());
        assertEquals(NotificationType.TECHNICIAN_ASSIGNED, empNotifs.get(0).getType());
        assertTrue(empNotifs.get(0).getMessage().contains("assigned to your ticket"));

        // Verify admin (actor) did NOT receive notification
        List<Notification> adminNotifs = notificationRepository.findByRecipientIdOrderByCreatedAtDescIdDesc(admin.getId());
        assertEquals(0, adminNotifs.size());
    }

    @Test
    @DisplayName("Duplicate technician assignment should not generate duplicate notifications")
    void testDuplicateTechnicianAssignmentPreventsDuplicateNotification() throws Exception
    {
        CreateTicketRequest req = new CreateTicketRequest("Monitor Flicker", "HDMI loose", "Hardware", TicketPriority.LOW, employee.getId());
        Ticket ticket = ticketService.createTicket(req);

        // First assignment
        mockMvc.perform(patch("/api/tickets/" + ticket.getId() + "/assign/" + technician.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        int initialCount = notificationRepository.findByRecipientIdOrderByCreatedAtDescIdDesc(technician.getId()).size();
        assertEquals(1, initialCount);

        // Repeat assignment with same technician
        mockMvc.perform(patch("/api/tickets/" + ticket.getId() + "/assign/" + technician.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Count must remain 1 (no duplicates)
        int finalCount = notificationRepository.findByRecipientIdOrderByCreatedAtDescIdDesc(technician.getId()).size();
        assertEquals(1, finalCount);
    }

    @Test
    @DisplayName("Status update should notify ticket creator and exclude the performing technician")
    void testStatusUpdateNotification() throws Exception
    {
        CreateTicketRequest req = new CreateTicketRequest("Password Reset", "Locked out", "Security", TicketPriority.MEDIUM, employee.getId());
        Ticket ticket = ticketService.createTicket(req);

        // Assign technician directly
        ticketService.assignTechnician(ticket.getId(), technician.getId());

        // Clear previous assignment notifications for cleaner assert
        notificationRepository.deleteAll();

        // Technician updates status to IN_PROGRESS via API
        mockMvc.perform(patch("/api/tickets/" + ticket.getId() + "/status?status=IN_PROGRESS")
                        .header("Authorization", "Bearer " + techToken))
                .andExpect(status().isOk());

        // Verify creator received STATUS_CHANGED notification
        List<Notification> empNotifs = notificationRepository.findByRecipientIdOrderByCreatedAtDescIdDesc(employee.getId());
        assertEquals(1, empNotifs.size());
        assertEquals(NotificationType.STATUS_CHANGED, empNotifs.get(0).getType());
        assertTrue(empNotifs.get(0).getMessage().contains("IN_PROGRESS"));

        // Technician (actor) should NOT receive a notification
        List<Notification> techNotifs = notificationRepository.findByRecipientIdOrderByCreatedAtDescIdDesc(technician.getId());
        assertEquals(0, techNotifs.size());
    }

    @Test
    @DisplayName("Resolving a ticket should generate TICKET_RESOLVED notification for creator")
    void testTicketResolvedNotification() throws Exception
    {
        CreateTicketRequest req = new CreateTicketRequest("Software install", "Need IntelliJ", "Software", TicketPriority.MEDIUM, employee.getId());
        Ticket ticket = ticketService.createTicket(req);
        ticketService.assignTechnician(ticket.getId(), technician.getId());
        notificationRepository.deleteAll();

        ResolutionRequest resReq = new ResolutionRequest("Installed and activated license.", true);

        // Technician resolves ticket via API
        mockMvc.perform(patch("/api/tickets/" + ticket.getId() + "/resolve")
                        .header("Authorization", "Bearer " + techToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resReq)))
                .andExpect(status().isOk());

        List<Notification> empNotifs = notificationRepository.findByRecipientIdOrderByCreatedAtDescIdDesc(employee.getId());
        assertEquals(1, empNotifs.size());
        assertEquals(NotificationType.TICKET_RESOLVED, empNotifs.get(0).getType());
        assertTrue(empNotifs.get(0).getMessage().contains("RESOLVED"));
    }

    @Test
    @DisplayName("Adding a comment should notify the other party and never the comment author")
    void testCommentNotification() throws Exception
    {
        CreateTicketRequest req = new CreateTicketRequest("Wifi drops", "Disconnection every 10 min", "Network", TicketPriority.HIGH, employee.getId());
        Ticket ticket = ticketService.createTicket(req);
        ticketService.assignTechnician(ticket.getId(), technician.getId());
        notificationRepository.deleteAll();

        // Technician comments on the ticket
        AddCommentRequest commentReq = new AddCommentRequest(technician.getId(), "Looking into access point logs.");
        mockMvc.perform(post("/api/tickets/" + ticket.getId() + "/comments")
                        .header("Authorization", "Bearer " + techToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentReq)))
                .andExpect(status().isCreated());

        // Employee receives COMMENT_ADDED
        List<Notification> empNotifs = notificationRepository.findByRecipientIdOrderByCreatedAtDescIdDesc(employee.getId());
        assertEquals(1, empNotifs.size());
        assertEquals(NotificationType.COMMENT_ADDED, empNotifs.get(0).getType());
        assertTrue(empNotifs.get(0).getMessage().contains("Tech User"));

        // Technician (author) does not receive a notification
        List<Notification> techNotifs = notificationRepository.findByRecipientIdOrderByCreatedAtDescIdDesc(technician.getId());
        assertEquals(0, techNotifs.size());
    }

    @Test
    @DisplayName("GET /api/notifications should retrieve current user's notifications newest first")
    void testGetNotificationsApi() throws Exception
    {
        // Create 2 notifications for employee
        notificationService.sendNotification(employee, null, "First Notice", "Notice 1 body", NotificationType.STATUS_CHANGED);
        notificationService.sendNotification(employee, null, "Second Notice", "Notice 2 body", NotificationType.COMMENT_ADDED);

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Second Notice")))
                .andExpect(jsonPath("$[1].title", is("First Notice")));
    }

    @Test
    @DisplayName("GET /api/notifications/unread-count should return accurate unread badge count")
    void testGetUnreadCountApi() throws Exception
    {
        notificationService.sendNotification(employee, null, "Note A", "A", NotificationType.STATUS_CHANGED);
        notificationService.sendNotification(employee, null, "Note B", "B", NotificationType.COMMENT_ADDED);

        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount", is(2)));
    }

    @Test
    @DisplayName("PATCH /api/notifications/{id}/read should mark notification as read")
    void testMarkAsReadSuccess() throws Exception
    {
        Notification notif = notificationService.sendNotification(employee, null, "Test Read", "Body", NotificationType.STATUS_CHANGED);
        assertFalse(notif.isRead());

        mockMvc.perform(patch("/api/notifications/" + notif.getId() + "/read")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(notif.getId().intValue())))
                .andExpect(jsonPath("$.read", is(true)));

        Notification updated = notificationRepository.findById(notif.getId()).orElseThrow();
        assertTrue(updated.isRead());
    }

    @Test
    @DisplayName("PATCH /api/notifications/{id}/read should return 403 Forbidden when modifying another user's notification")
    void testMarkAsReadOtherUserForbidden() throws Exception
    {
        // Notification belongs to employee
        Notification notif = notificationService.sendNotification(employee, null, "Private Note", "Body", NotificationType.STATUS_CHANGED);

        // Technician tries to mark it as read
        mockMvc.perform(patch("/api/notifications/" + notif.getId() + "/read")
                        .header("Authorization", "Bearer " + techToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /api/notifications/read-all should mark all notifications for current user as read")
    void testMarkAllAsRead() throws Exception
    {
        notificationService.sendNotification(employee, null, "Note 1", "Body 1", NotificationType.STATUS_CHANGED);
        notificationService.sendNotification(employee, null, "Note 2", "Body 2", NotificationType.STATUS_CHANGED);

        // Notification for technician should remain unchanged
        Notification techNotif = notificationService.sendNotification(technician, null, "Tech Note", "Body", NotificationType.STATUS_CHANGED);

        mockMvc.perform(patch("/api/notifications/read-all")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updatedCount", is(2)));

        long unreadEmp = notificationRepository.countByRecipientIdAndReadFalse(employee.getId());
        assertEquals(0, unreadEmp);

        // Technician's unread notification should remain
        long unreadTech = notificationRepository.countByRecipientIdAndReadFalse(technician.getId());
        assertEquals(1, unreadTech);
    }

    @Test
    @DisplayName("Unauthenticated request to notifications should return 401 Unauthorized")
    void testUnauthenticatedAccessRejected() throws Exception
    {
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/notifications/unread-count"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(patch("/api/notifications/1/read"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(patch("/api/notifications/read-all"))
                .andExpect(status().isUnauthorized());
    }
}