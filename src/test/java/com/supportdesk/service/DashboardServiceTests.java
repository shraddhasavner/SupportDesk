package com.supportdesk.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.supportdesk.controller.CreateTicketRequest;
import com.supportdesk.controller.DashboardSummaryResponse;
import com.supportdesk.controller.DateRangeReportResponse;
import com.supportdesk.controller.PrioritySummaryResponse;
import com.supportdesk.controller.TechnicianWorkloadResponse;
import com.supportdesk.entity.Employee;
import com.supportdesk.entity.Role;
import com.supportdesk.entity.Ticket;
import com.supportdesk.entity.TicketPriority;
import com.supportdesk.entity.TicketStatus;
import com.supportdesk.repository.EmployeeRepository;
import com.supportdesk.repository.TicketRepository;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class DashboardServiceTests
{
    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    @DisplayName("Should return valid metrics for mixed ticket statuses and overdue tracking")
    void testMixedTicketStatusesSummary() 
    {
        Employee creator = new Employee("Emp Test", "emp.test@test.com", "password123", "IT", Role.EMPLOYEE, "111");
        Employee tech = new Employee("Tech Test", "tech.test@test.com", "password123", "IT", Role.TECHNICIAN, "222");
        employeeRepository.save(creator);
        employeeRepository.save(tech);

        Ticket t1 = ticketService.createTicket(new CreateTicketRequest("Open T1", "Desc", "SW", TicketPriority.LOW, creator.getId()));

        Ticket t2 = ticketService.createTicket(new CreateTicketRequest("Assigned T2", "Desc", "HW", TicketPriority.MEDIUM, creator.getId()));
        ticketService.assignTechnician(t2.getId(), tech.getId());

        Ticket t3 = ticketService.createTicket(new CreateTicketRequest("Progress T3", "Desc", "NW", TicketPriority.HIGH, creator.getId()));
        ticketService.updateTicketStatus(t3.getId(), TicketStatus.IN_PROGRESS);

        Ticket t4 = ticketService.createTicket(new CreateTicketRequest("Resolved T4", "Desc", "HW", TicketPriority.CRITICAL, creator.getId()));
        ticketService.addResolutionNotes(t4.getId(), "Fixed issue", true);

        Ticket t5 = ticketService.createTicket(new CreateTicketRequest("Closed T5", "Desc", "SW", TicketPriority.LOW, creator.getId()));
        ticketService.updateTicketStatus(t5.getId(), TicketStatus.CLOSED);

        Ticket t6 = ticketService.createTicket(new CreateTicketRequest("Overdue T6", "Desc", "HW", TicketPriority.CRITICAL, creator.getId()));
        t6.setSlaDeadline(LocalDateTime.now().minusHours(2));
        ticketRepository.save(t6);

        DashboardSummaryResponse summary = dashboardService.getDashboardSummary();
        assertNotNull(summary);
        assertTrue(summary.getTotalTickets() >= 6);
        assertTrue(summary.getOpenTickets() >= 2);
        assertTrue(summary.getAssignedTickets() >= 1);
        assertTrue(summary.getInProgressTickets() >= 1);
        assertTrue(summary.getResolvedTickets() >= 1);
        assertTrue(summary.getClosedTickets() >= 1);
        assertTrue(summary.getOverdueTickets() >= 1);
    }

    @Test
    @DisplayName("Should return accurate priority breakdown counts")
    void testPriorityBreakdownSummary() 
    {
        Employee creator = new Employee("Priority User", "priority.user@test.com", "password123", "Finance", Role.EMPLOYEE, "333");
        employeeRepository.save(creator);

        ticketService.createTicket(new CreateTicketRequest("P Low", "Desc", "SW", TicketPriority.LOW, creator.getId()));
        ticketService.createTicket(new CreateTicketRequest("P Med", "Desc", "SW", TicketPriority.MEDIUM, creator.getId()));
        ticketService.createTicket(new CreateTicketRequest("P High", "Desc", "SW", TicketPriority.HIGH, creator.getId()));
        ticketService.createTicket(new CreateTicketRequest("P Crit", "Desc", "SW", TicketPriority.CRITICAL, creator.getId()));

        PrioritySummaryResponse prioritySummary = dashboardService.getPrioritySummary();
        assertNotNull(prioritySummary);
        assertTrue(prioritySummary.getLow() >= 1);
        assertTrue(prioritySummary.getMedium() >= 1);
        assertTrue(prioritySummary.getHigh() >= 1);
        assertTrue(prioritySummary.getCritical() >= 1);
    }

    @Test
    @DisplayName("Should calculate workload only for active technicians (assigned, pending, resolved)")
    void testTechnicianWorkload() 
    {
        Employee creator = new Employee("Workload Creator", "creator.workload@test.com", "password123", "HR", Role.EMPLOYEE, "444");
        Employee activeTech = new Employee("Active Tech", "active.tech@test.com", "password123", "IT Support", Role.TECHNICIAN, "555");
        Employee inactiveTech = new Employee("Inactive Tech", "inactive.tech@test.com", "password123", "IT Support", Role.TECHNICIAN, "666");
        inactiveTech.setActive(false);

        employeeRepository.save(creator);
        employeeRepository.save(activeTech);
        employeeRepository.save(inactiveTech);

        Ticket t1 = ticketService.createTicket(new CreateTicketRequest("Workload T1", "Desc", "HW", TicketPriority.MEDIUM, creator.getId()));
        ticketService.assignTechnician(t1.getId(), activeTech.getId());

        Ticket t2 = ticketService.createTicket(new CreateTicketRequest("Workload T2", "Desc", "SW", TicketPriority.HIGH, creator.getId()));
        ticketService.assignTechnician(t2.getId(), activeTech.getId());
        ticketService.updateTicketStatus(t2.getId(), TicketStatus.IN_PROGRESS);

        Ticket t3 = ticketService.createTicket(new CreateTicketRequest("Workload T3", "Desc", "NW", TicketPriority.LOW, creator.getId()));
        ticketService.assignTechnician(t3.getId(), activeTech.getId());
        ticketService.addResolutionNotes(t3.getId(), "Resolved network cable issue", true);

        List<TechnicianWorkloadResponse> workloads = dashboardService.getTechnicianWorkload();
        assertNotNull(workloads);

        assertTrue(workloads.stream().noneMatch(w -> w.getTechnicianId().equals(inactiveTech.getId())));

        TechnicianWorkloadResponse activeWorkload = workloads.stream()
                .filter(w -> w.getTechnicianId().equals(activeTech.getId()))
                .findFirst()
                .orElse(null);

        assertNotNull(activeWorkload, "Active technician workload must be present");
        assertEquals(3L, activeWorkload.getAssignedTicketCount());
        assertEquals(2L, activeWorkload.getPendingTicketCount());
        assertEquals(1L, activeWorkload.getResolvedTicketCount());
    }

    @Test
    @DisplayName("Should generate accurate date-range reports and validate boundary dates")
    void testDateRangeReports() 
    {
        Employee creator = new Employee("Report User", "report.user@test.com", "password123", "Sales", Role.EMPLOYEE, "777");
        employeeRepository.save(creator);

        Ticket t1 = ticketService.createTicket(new CreateTicketRequest("Report T1", "Desc", "SW", TicketPriority.LOW, creator.getId()));
        Ticket t2 = ticketService.createTicket(new CreateTicketRequest("Report T2", "Desc", "HW", TicketPriority.HIGH, creator.getId()));
        ticketService.addResolutionNotes(t2.getId(), "Fixed hardware", true);

        LocalDate today = LocalDate.now();
        DateRangeReportResponse report = dashboardService.getDateRangeReport(today.minusDays(1), today.plusDays(1));

        assertNotNull(report);
        assertTrue(report.getTotalCreated() >= 2);
        assertTrue(report.getTotalResolved() >= 1);

        assertThrows(IllegalArgumentException.class, () ->
                dashboardService.getDateRangeReport(today.plusDays(5), today));
    }

    @Test
    @DisplayName("Should correctly handle active technicians with zero assigned tickets")
    void testTechnicianWithZeroAssignedTickets() 
    {
        Employee idleTech = new Employee("Idle Tech", "idle.tech@test.com", "password123", "IT Support", Role.TECHNICIAN, "9990");
        employeeRepository.save(idleTech);

        List<TechnicianWorkloadResponse> workloads = dashboardService.getTechnicianWorkload();
        TechnicianWorkloadResponse workload = workloads.stream()
                .filter(w -> w.getTechnicianId().equals(idleTech.getId()))
                .findFirst()
                .orElse(null);

        assertNotNull(workload, "Idle technician must be present in workload list");
        assertEquals(0L, workload.getAssignedTicketCount());
        assertEquals(0L, workload.getPendingTicketCount());
        assertEquals(0L, workload.getResolvedTicketCount());
        assertEquals("Idle Tech", workload.getTechnicianName());
        assertEquals("IT Support", workload.getDepartment());
    }

    @Test
    @DisplayName("Should not double-count tickets when ticket has multiple comments and activities")
    void testTechnicianWorkloadNoDoubleCountingWithMultipleCommentsAndActivities() 
    {
        Employee creator = new Employee("DoubleCount User", "creator.dc@test.com", "password123", "Finance", Role.EMPLOYEE, "8880");
        Employee tech = new Employee("DoubleCount Tech", "tech.dc@test.com", "password123", "IT", Role.TECHNICIAN, "8881");
        employeeRepository.save(creator);
        employeeRepository.save(tech);

        Ticket ticket = ticketService.createTicket(new CreateTicketRequest("DoubleCount Issue", "Checking join multiplication", "Software", TicketPriority.HIGH, creator.getId()));
        ticketService.assignTechnician(ticket.getId(), tech.getId());

        ticketService.addComment(ticket.getId(), creator.getId(), "Comment 1");
        ticketService.addComment(ticket.getId(), tech.getId(), "Comment 2");
        ticketService.addComment(ticket.getId(), creator.getId(), "Comment 3");

        ticketService.updateTicketStatus(ticket.getId(), TicketStatus.IN_PROGRESS);

        List<TechnicianWorkloadResponse> workloads = dashboardService.getTechnicianWorkload();
        TechnicianWorkloadResponse workload = workloads.stream()
                .filter(w -> w.getTechnicianId().equals(tech.getId()))
                .findFirst()
                .orElse(null);

        assertNotNull(workload, "Workload must be present");
        assertEquals(1L, workload.getAssignedTicketCount(), "Ticket must not be double counted due to comments or activities");
        assertEquals(1L, workload.getPendingTicketCount(), "Pending count must remain 1");
        assertEquals(0L, workload.getResolvedTicketCount(), "Resolved count must remain 0");
    }

    @Test
    @DisplayName("Should execute strictly 1 SQL query regardless of technician count (O(1) query complexity)")
    void testTechnicianWorkloadQueryCountConstant() 
    {
        for (int i = 1; i <= 4; i++) 
        {
            Employee tech = new Employee("Batch Tech " + i, "batch.tech" + i + "@test.com", "password123", "IT", Role.TECHNICIAN, "999" + i);
            employeeRepository.save(tech);
        }

        org.hibernate.SessionFactory sessionFactory = entityManagerFactory.unwrap(org.hibernate.SessionFactory.class);
        sessionFactory.getStatistics().setStatisticsEnabled(true);
        sessionFactory.getStatistics().clear();

        List<TechnicianWorkloadResponse> workloads = dashboardService.getTechnicianWorkload();

        long queryCount = sessionFactory.getStatistics().getPrepareStatementCount();
        assertTrue(workloads.size() >= 4, "Should retrieve all active technicians");
        assertEquals(1L, queryCount, "Optimized workload query must execute exactly 1 SQL statement instead of 1 + 3N");
    }
}