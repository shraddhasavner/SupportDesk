package com.supportdesk.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.supportdesk.controller.AddCommentRequest;
import com.supportdesk.controller.CreateTicketRequest;
import com.supportdesk.entity.ActivityType;
import com.supportdesk.entity.Employee;
import com.supportdesk.entity.Role;
import com.supportdesk.entity.Ticket;
import com.supportdesk.entity.TicketActivity;
import com.supportdesk.entity.TicketComment;
import com.supportdesk.entity.TicketPriority;
import com.supportdesk.entity.TicketStatus;
import com.supportdesk.repository.EmployeeRepository;
import com.supportdesk.repository.TicketActivityRepository;
import com.supportdesk.repository.TicketCommentRepository;
import com.supportdesk.repository.TicketRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class TicketCommentAndActivityTests
{
    @Autowired
    private TicketService ticketService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketCommentRepository ticketCommentRepository;

    @Autowired
    private TicketActivityRepository ticketActivityRepository;

    @Test
    @DisplayName("Should add comment to ticket successfully and record COMMENT_ADDED activity")
    void testAddCommentSuccess()
    {
        Employee user = new Employee("Alok Sharma", "alok@test.com", "password123", "IT", Role.EMPLOYEE, "111222");
        employeeRepository.save(user);

        Ticket ticket = ticketService.createTicket(new CreateTicketRequest("Printer issue", "Toner low", "Hardware", TicketPriority.LOW, user.getId()));

        AddCommentRequest commentReq = new AddCommentRequest(user.getId(), "Ordered toner replacement from supplier.");
        TicketComment savedComment = ticketService.addComment(ticket.getId(), commentReq);

        assertNotNull(savedComment.getId());
        assertEquals("Ordered toner replacement from supplier.", savedComment.getCommentText());
        assertEquals(user.getId(), savedComment.getAuthor().getId());
        assertEquals(ticket.getId(), savedComment.getTicket().getId());
        assertNotNull(savedComment.getCreatedAt());

        List<TicketActivity> activities = ticketService.getTicketActivities(ticket.getId());
        assertTrue(activities.stream().anyMatch(a -> a.getActivityType() == ActivityType.COMMENT_ADDED));
    }

    @Test
    @DisplayName("Should fail adding comment if ticket does not exist (404 ResourceNotFound)")
    void testAddCommentTicketNotFoundFails()
    {
        Employee user = new Employee("Bhavna Lal", "bhavna@test.com", "password123", "HR", Role.EMPLOYEE, "333444");
        employeeRepository.save(user);

        AddCommentRequest commentReq = new AddCommentRequest(user.getId(), "Some comment");
        assertThrows(ResourceNotFoundException.class, () -> ticketService.addComment(999999L, commentReq));
    }

    @Test
    @DisplayName("Should fail adding comment if author employee does not exist (404 ResourceNotFound)")
    void testAddCommentAuthorNotFoundFails()
    {
        Employee user = new Employee("Chetan Roy", "chetan@test.com", "password123", "Finance", Role.EMPLOYEE, "555666");
        employeeRepository.save(user);

        Ticket ticket = ticketService.createTicket(new CreateTicketRequest("Keyboard fault", "Space key stuck", "Hardware", TicketPriority.LOW, user.getId()));

        AddCommentRequest commentReq = new AddCommentRequest(888888L, "Non-existent employee comment");
        assertThrows(ResourceNotFoundException.class, () -> ticketService.addComment(ticket.getId(), commentReq));
    }

    @Test
    @DisplayName("Should fail adding comment if author employee is inactive")
    void testAddCommentInactiveAuthorFails()
    {
        Employee user = new Employee("Active Creator", "creator.active@test.com", "password123", "IT", Role.EMPLOYEE, "777888");
        Employee inactiveUser = new Employee("Inactive Author", "author.inactive@test.com", "password123", "IT", Role.EMPLOYEE, "999000");
        inactiveUser.setActive(false);
        employeeRepository.save(user);
        employeeRepository.save(inactiveUser);

        Ticket ticket = ticketService.createTicket(new CreateTicketRequest("Mouse lag", "Cursor stuttering", "Hardware", TicketPriority.LOW, user.getId()));

        AddCommentRequest commentReq = new AddCommentRequest(inactiveUser.getId(), "Trying to post as inactive employee");
        assertThrows(IllegalArgumentException.class, () -> ticketService.addComment(ticket.getId(), commentReq));
    }

    @Test
    @DisplayName("Should fail adding comment if comment text is blank")
    void testAddCommentBlankTextFails()
    {
        Employee user = new Employee("Divya Joshi", "divya@test.com", "password123", "Marketing", Role.EMPLOYEE, "123123");
        employeeRepository.save(user);

        Ticket ticket = ticketService.createTicket(new CreateTicketRequest("Monitor flickering", "HDMI port issue", "Hardware", TicketPriority.MEDIUM, user.getId()));

        AddCommentRequest blankReq = new AddCommentRequest(user.getId(), "   ");
        assertThrows(IllegalArgumentException.class, () -> ticketService.addComment(ticket.getId(), blankReq));
    }

    @Test
    @DisplayName("Should return comments for ticket ordered chronologically (oldest first)")
    void testGetCommentsChronologicalOrder() throws InterruptedException
    {
        Employee user = new Employee("Esha Gupta", "esha@test.com", "password123", "Operations", Role.EMPLOYEE, "456456");
        Employee tech = new Employee("Farhan Tech", "farhan@test.com", "password123", "IT", Role.TECHNICIAN, "789789");
        employeeRepository.save(user);
        employeeRepository.save(tech);

        Ticket ticket = ticketService.createTicket(new CreateTicketRequest("VPN connection failure", "Error code 809", "Network", TicketPriority.HIGH, user.getId()));

        TicketComment c1 = ticketService.addComment(ticket.getId(), user.getId(), "First comment: Trying to reconnect from home.");
        Thread.sleep(10);
        TicketComment c2 = ticketService.addComment(ticket.getId(), tech.getId(), "Second comment: Looking into the firewall logs.");
        Thread.sleep(10);
        TicketComment c3 = ticketService.addComment(ticket.getId(), user.getId(), "Third comment: Now it shows error 691.");

        List<TicketComment> comments = ticketService.getCommentsByTicket(ticket.getId());
        assertEquals(3, comments.size());
        assertEquals(c1.getId(), comments.get(0).getId());
        assertEquals(c2.getId(), comments.get(1).getId());
        assertEquals(c3.getId(), comments.get(2).getId());
        assertEquals("First comment: Trying to reconnect from home.", comments.get(0).getCommentText());
        assertEquals("Third comment: Now it shows error 691.", comments.get(2).getCommentText());
    }

    @Test
    @DisplayName("Should fail listing comments if ticket does not exist (404)")
    void testGetCommentsTicketNotFoundFails()
    {
        assertThrows(ResourceNotFoundException.class, () -> ticketService.getCommentsByTicket(777777L));
    }

    @Test
    @DisplayName("Should record complete activity lifecycle and prevent duplicate activity logs")
    void testActivityHistoryLifecycleAndDuplicatePrevention()
    {
        Employee creator = new Employee("Gita Verma", "gita@test.com", "password123", "Accounts", Role.EMPLOYEE, "321321");
        Employee tech1 = new Employee("Harish Tech", "harish@test.com", "password123", "IT", Role.TECHNICIAN, "654654");
        employeeRepository.save(creator);
        employeeRepository.save(tech1);

        Ticket ticket = ticketService.createTicket(new CreateTicketRequest("Server slow", "Database latency high", "Software", TicketPriority.CRITICAL, creator.getId()));
        List<TicketActivity> actAfterCreate = ticketService.getTicketActivities(ticket.getId());
        assertEquals(1, actAfterCreate.size());
        assertEquals(ActivityType.TICKET_CREATED, actAfterCreate.get(0).getActivityType());

        ticketService.assignTechnician(ticket.getId(), tech1.getId());
        List<TicketActivity> actAfterAssign = ticketService.getTicketActivities(ticket.getId());
        assertEquals(2, actAfterAssign.size());
        assertEquals(ActivityType.TECHNICIAN_ASSIGNED, actAfterAssign.get(1).getActivityType());

        ticketService.assignTechnician(ticket.getId(), tech1.getId());
        List<TicketActivity> actAfterDuplicateAssign = ticketService.getTicketActivities(ticket.getId());
        assertEquals(2, actAfterDuplicateAssign.size(), "Duplicate technician assignment must NOT create new activity record");

        ticketService.updateTicketStatus(ticket.getId(), TicketStatus.IN_PROGRESS);
        List<TicketActivity> actAfterStatus = ticketService.getTicketActivities(ticket.getId());
        assertEquals(3, actAfterStatus.size());
        assertEquals(ActivityType.STATUS_CHANGED, actAfterStatus.get(2).getActivityType());

        ticketService.updateTicketStatus(ticket.getId(), TicketStatus.IN_PROGRESS);
        List<TicketActivity> actAfterDuplicateStatus = ticketService.getTicketActivities(ticket.getId());
        assertEquals(3, actAfterDuplicateStatus.size(), "Duplicate status update must NOT create new activity record");

        ticketService.addComment(ticket.getId(), tech1.getId(), "Optimized PostgreSQL buffer pool.");
        List<TicketActivity> actAfterComment = ticketService.getTicketActivities(ticket.getId());
        assertEquals(4, actAfterComment.size());
        assertEquals(ActivityType.COMMENT_ADDED, actAfterComment.get(3).getActivityType());

        ticketService.addResolutionNotes(ticket.getId(), "Indexes created and cache tuned.", true);
        List<TicketActivity> actAfterResolve = ticketService.getTicketActivities(ticket.getId());
        assertEquals(5, actAfterResolve.size());
        assertEquals(ActivityType.TICKET_RESOLVED, actAfterResolve.get(4).getActivityType());

        ticketService.addResolutionNotes(ticket.getId(), "Indexes created and cache tuned.", true);
        List<TicketActivity> actAfterDuplicateResolve = ticketService.getTicketActivities(ticket.getId());
        assertEquals(5, actAfterDuplicateResolve.size(), "Duplicate resolution must NOT create new activity record");

        assertEquals(ActivityType.TICKET_CREATED, actAfterDuplicateResolve.get(0).getActivityType());
        assertEquals(ActivityType.TECHNICIAN_ASSIGNED, actAfterDuplicateResolve.get(1).getActivityType());
        assertEquals(ActivityType.STATUS_CHANGED, actAfterDuplicateResolve.get(2).getActivityType());
        assertEquals(ActivityType.COMMENT_ADDED, actAfterDuplicateResolve.get(3).getActivityType());
        assertEquals(ActivityType.TICKET_RESOLVED, actAfterDuplicateResolve.get(4).getActivityType());
    }

    @Test
    @DisplayName("Should fail listing activities if ticket does not exist (404)")
    void testGetActivitiesTicketNotFoundFails()
    {
        assertThrows(ResourceNotFoundException.class, () -> ticketService.getTicketActivities(555555L));
    }
}