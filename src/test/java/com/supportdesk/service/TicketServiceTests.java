package com.supportdesk.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.supportdesk.controller.CreateTicketRequest;
import com.supportdesk.controller.SlaStatusResponse;
import com.supportdesk.entity.ActivityType;
import com.supportdesk.entity.Employee;
import com.supportdesk.entity.Role;
import com.supportdesk.entity.Ticket;
import com.supportdesk.entity.TicketActivity;
import com.supportdesk.entity.TicketPriority;
import com.supportdesk.entity.TicketStatus;
import com.supportdesk.repository.EmployeeRepository;
import com.supportdesk.repository.TicketRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class TicketServiceTests
{
    @Autowired
    private TicketService ticketService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Test
    @DisplayName("Should create ticket with generated unique ticket number")
    void testCreateTicket()
    {
        Employee user = new Employee("Deepak Sen", "deepak@test.com", "password123", "Finance", Role.EMPLOYEE, "123456");
        employeeRepository.save(user);

        CreateTicketRequest req = new CreateTicketRequest("Cannot access ERP", "Error 403 on login", "Software", TicketPriority.HIGH, user.getId());
        Ticket created = ticketService.createTicket(req);

        assertNotNull(created.getId());
        assertNotNull(created.getTicketNumber());
        assertTrue(created.getTicketNumber().startsWith("TCK-"));
        assertEquals(TicketStatus.OPEN, created.getStatus());
        assertEquals("Cannot access ERP", created.getTitle());
    }

    @Test
    @DisplayName("Should fail ticket creation if creator is inactive")
    void testCreateTicketInactiveEmployeeFails()
    {
        Employee inactiveUser = new Employee("Inactive Creator", "inactive.creator@test.com", "password123", "HR", Role.EMPLOYEE, "123456");
        inactiveUser.setActive(false);
        employeeRepository.save(inactiveUser);

        CreateTicketRequest req = new CreateTicketRequest("Printer issue", "No toner", "Hardware", TicketPriority.LOW, inactiveUser.getId());
        assertThrows(IllegalArgumentException.class, () -> ticketService.createTicket(req));
    }

    @Test
    @DisplayName("Should assign technician successfully and update status to ASSIGNED")
    void testAssignTechnicianSuccess()
    {
        Employee user = new Employee("Ravi Kumar", "ravi@test.com", "password123", "Sales", Role.EMPLOYEE, "123");
        Employee tech = new Employee("Neha IT Tech", "neha.tech@test.com", "password123", "IT", Role.TECHNICIAN, "999");
        employeeRepository.save(user);
        employeeRepository.save(tech);

        CreateTicketRequest req = new CreateTicketRequest("Monitor flickering", "HDMI issue", "Hardware", TicketPriority.MEDIUM, user.getId());
        Ticket ticket = ticketService.createTicket(req);

        Ticket assignedTicket = ticketService.assignTechnician(ticket.getId(), tech.getId());
        assertEquals(TicketStatus.ASSIGNED, assignedTicket.getStatus());
        assertEquals("Neha IT Tech", assignedTicket.getAssignedTechnician().getName());
    }

    @Test
    @DisplayName("Should record administrator as activity actor on technician assignment")
    void testAssignTechnicianRecordsAdminAsActivityActor()
    {
        Employee user = new Employee("End User", "enduser@test.com", "password123", "Sales", Role.EMPLOYEE, "123");
        Employee tech = new Employee("Field Tech", "field.tech@test.com", "password123", "IT", Role.TECHNICIAN, "999");
        Employee admin = new Employee("Admin Boss", "admin.boss@test.com", "password123", "IT", Role.ADMIN, "000");
        employeeRepository.save(user);
        employeeRepository.save(tech);
        employeeRepository.save(admin);

        CreateTicketRequest req = new CreateTicketRequest("Printer Jam", "Tray 2 jammed", "Hardware", TicketPriority.LOW, user.getId());
        Ticket ticket = ticketService.createTicket(req);

        Ticket assigned = ticketService.assignTechnician(ticket.getId(), tech.getId(), admin);
        assertEquals(TicketStatus.ASSIGNED, assigned.getStatus());
        assertEquals(tech.getId(), assigned.getAssignedTechnician().getId(), "Technician must be the assignment target");

        List<TicketActivity> activities = ticketService.getTicketActivities(ticket.getId());
        assertEquals(2, activities.size());
        TicketActivity assignActivity = activities.get(1);
        assertEquals(ActivityType.TECHNICIAN_ASSIGNED, assignActivity.getActivityType());
        assertNotNull(assignActivity.getPerformedBy());
        assertEquals(admin.getId(), assignActivity.getPerformedBy().getId(), "Activity actor must be the administrator who performed the assignment");
        assertEquals("Admin Boss", assignActivity.getPerformedBy().getName());
        assertEquals("Ticket assigned to technician Field Tech", assignActivity.getDescription());
    }

    @Test
    @DisplayName("Should fail assignment if employee does not have TECHNICIAN role")
    void testAssignNonTechnicianFails()
    {
        Employee user = new Employee("User A", "userA@test.com", "password123", "Sales", Role.EMPLOYEE, "123");
        Employee nonTech = new Employee("User B", "userB@test.com", "password123", "HR", Role.EMPLOYEE, "456");
        employeeRepository.save(user);
        employeeRepository.save(nonTech);

        CreateTicketRequest req = new CreateTicketRequest("Email bounce", "Cannot send mails", "Software", TicketPriority.MEDIUM, user.getId());
        Ticket ticket = ticketService.createTicket(req);

        assertThrows(IllegalArgumentException.class, () -> ticketService.assignTechnician(ticket.getId(), nonTech.getId()));
    }

    @Test
    @DisplayName("Should update ticket status and add resolution notes")
    void testUpdateStatusAndResolve()
    {
        Employee user = new Employee("User C", "userC@test.com", "password123", "IT", Role.EMPLOYEE, "123");
        employeeRepository.save(user);

        CreateTicketRequest req = new CreateTicketRequest("Keyboard broken", "Keys not working", "Hardware", TicketPriority.LOW, user.getId());
        Ticket ticket = ticketService.createTicket(req);

        Ticket inProgress = ticketService.updateTicketStatus(ticket.getId(), TicketStatus.IN_PROGRESS);
        assertEquals(TicketStatus.IN_PROGRESS, inProgress.getStatus());

        Ticket resolved = ticketService.addResolutionNotes(ticket.getId(), "Replaced with new mechanical keyboard.", true);
        assertEquals(TicketStatus.RESOLVED, resolved.getStatus());
        assertEquals("Replaced with new mechanical keyboard.", resolved.getResolutionNotes());
    }

    @Test
    @DisplayName("Should automatically calculate SLA deadlines for all priorities (4h, 8h, 24h, 48h)")
    void testSlaDeadlineCalculationForAllPriorities()
    {
        LocalDateTime baseTime = LocalDateTime.of(2026, 10, 14, 10, 0);

        LocalDateTime criticalDeadline = ticketService.calculateSlaDeadline(TicketPriority.CRITICAL, baseTime);
        assertEquals(baseTime.plusHours(4), criticalDeadline, "CRITICAL priority must have 4-hour SLA");

        LocalDateTime highDeadline = ticketService.calculateSlaDeadline(TicketPriority.HIGH, baseTime);
        assertEquals(baseTime.plusHours(8), highDeadline, "HIGH priority must have 8-hour SLA");

        LocalDateTime mediumDeadline = ticketService.calculateSlaDeadline(TicketPriority.MEDIUM, baseTime);
        assertEquals(baseTime.plusHours(24), mediumDeadline, "MEDIUM priority must have 24-hour SLA");

        LocalDateTime lowDeadline = ticketService.calculateSlaDeadline(TicketPriority.LOW, baseTime);
        assertEquals(baseTime.plusHours(48), lowDeadline, "LOW priority must have 48-hour SLA");
    }

    @Test
    @DisplayName("Should identify overdue tickets when deadline has passed and ticket is not resolved")
    void testOverdueTicketDetected()
    {
        Employee user = new Employee("User D", "userD@test.com", "password123", "IT", Role.EMPLOYEE, "123");
        employeeRepository.save(user);

        CreateTicketRequest req = new CreateTicketRequest("Server fan failure", "Loud noise", "Hardware", TicketPriority.HIGH, user.getId());
        Ticket ticket = ticketService.createTicket(req);

        ticket.setSlaDeadline(LocalDateTime.now().minusHours(2));
        ticketRepository.save(ticket);

        assertTrue(ticket.isOverdue(), "Ticket with past deadline and OPEN status must be overdue");

        List<Ticket> overdueList = ticketService.getOverdueTickets();
        assertTrue(overdueList.stream().anyMatch(t -> t.getId().equals(ticket.getId())),
                "Overdue list must contain the ticket with passed deadline");
    }

    @Test
    @DisplayName("Should NOT mark ticket as overdue if it is RESOLVED, even if deadline has passed")
    void testResolvedTicketNotOverdue()
    {
        Employee user = new Employee("User E", "userE@test.com", "password123", "HR", Role.EMPLOYEE, "123");
        employeeRepository.save(user);

        CreateTicketRequest req = new CreateTicketRequest("Email sync", "Sync lag", "Software", TicketPriority.MEDIUM, user.getId());
        Ticket ticket = ticketService.createTicket(req);

        ticket.setSlaDeadline(LocalDateTime.now().minusHours(5));
        ticket.setStatus(TicketStatus.RESOLVED);
        ticketRepository.save(ticket);

        assertFalse(ticket.isOverdue(), "RESOLVED ticket must not be overdue");

        List<Ticket> overdueList = ticketService.getOverdueTickets();
        assertFalse(overdueList.stream().anyMatch(t -> t.getId().equals(ticket.getId())),
                "Overdue list must NOT contain RESOLVED tickets");
    }

    @Test
    @DisplayName("Should NOT mark ticket as overdue if it is CLOSED, even if deadline has passed")
    void testClosedTicketNotOverdue()
    {
        Employee user = new Employee("User F", "userF@test.com", "password123", "Finance", Role.EMPLOYEE, "123");
        employeeRepository.save(user);

        CreateTicketRequest req = new CreateTicketRequest("VPN reconnect", "VPN drop", "Network", TicketPriority.LOW, user.getId());
        Ticket ticket = ticketService.createTicket(req);

        ticket.setSlaDeadline(LocalDateTime.now().minusHours(10));
        ticket.setStatus(TicketStatus.CLOSED);
        ticketRepository.save(ticket);

        assertFalse(ticket.isOverdue(), "CLOSED ticket must not be overdue");

        List<Ticket> overdueList = ticketService.getOverdueTickets();
        assertFalse(overdueList.stream().anyMatch(t -> t.getId().equals(ticket.getId())),
                "Overdue list must NOT contain CLOSED tickets");
    }

    @Test
    @DisplayName("Should retrieve SLA status response with correct metrics")
    void testTicketSlaStatusResponse()
    {
        Employee user = new Employee("User G", "userG@test.com", "password123", "IT", Role.EMPLOYEE, "123");
        employeeRepository.save(user);

        CreateTicketRequest req = new CreateTicketRequest("Access to Jira", "Need board access", "Access", TicketPriority.CRITICAL, user.getId());
        Ticket ticket = ticketService.createTicket(req);

        SlaStatusResponse slaStatus = ticketService.getTicketSlaStatus(ticket.getId());
        assertNotNull(slaStatus);
        assertEquals(ticket.getId(), slaStatus.getTicketId());
        assertEquals(TicketPriority.CRITICAL, slaStatus.getPriority());
        assertEquals(4L, slaStatus.getTotalSlaHours());
        assertFalse(slaStatus.isOverdue());
        assertNotNull(slaStatus.getSlaDeadline());
    }
}