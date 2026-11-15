package com.supportdesk.service;

import com.supportdesk.controller.DashboardSummaryResponse;
import com.supportdesk.controller.DateRangeReportResponse;
import com.supportdesk.controller.PrioritySummaryResponse;
import com.supportdesk.controller.TechnicianWorkloadResponse;
import com.supportdesk.entity.Employee;
import com.supportdesk.entity.Role;
import com.supportdesk.entity.TicketPriority;
import com.supportdesk.entity.TicketStatus;
import com.supportdesk.repository.EmployeeRepository;
import com.supportdesk.repository.TicketRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * DashboardService: Business logic for help desk statistics and reports.
 * 
 * [English]:
 * - Uses database-level COUNT queries to avoid in-memory overhead.
 * - Computes overall ticket counts, priority breakdown, technician workloads, and date-range metrics.
 * 
 * [Hinglish]:
 * - Saare tickets ko memory me load karne ke bajaye direct MySQL se COUNT query chalaata hai taaki system fast rahe.
 * - Overall status counts, priority distribution, technician workload aur date range reports generate karta hai.
 */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final TicketRepository ticketRepository;
    private final EmployeeRepository employeeRepository;

    public DashboardService(TicketRepository ticketRepository, EmployeeRepository employeeRepository) {
        this.ticketRepository = ticketRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Overall dashboard summary with status counts and overdue metrics.
     */
    public DashboardSummaryResponse getDashboardSummary() {
        long total = ticketRepository.count();
        long open = ticketRepository.countByStatus(TicketStatus.OPEN);
        long assigned = ticketRepository.countByStatus(TicketStatus.ASSIGNED);
        long inProgress = ticketRepository.countByStatus(TicketStatus.IN_PROGRESS);
        long resolved = ticketRepository.countByStatus(TicketStatus.RESOLVED);
        long closed = ticketRepository.countByStatus(TicketStatus.CLOSED);
        long overdue = ticketRepository.countOverdueTickets(LocalDateTime.now());

        return new DashboardSummaryResponse(total, open, assigned, inProgress, resolved, closed, overdue);
    }

    /**
     * Priority distribution breakdown.
     */
    public PrioritySummaryResponse getPrioritySummary() {
        long low = ticketRepository.countByPriority(TicketPriority.LOW);
        long medium = ticketRepository.countByPriority(TicketPriority.MEDIUM);
        long high = ticketRepository.countByPriority(TicketPriority.HIGH);
        long critical = ticketRepository.countByPriority(TicketPriority.CRITICAL);

        return new PrioritySummaryResponse(low, medium, high, critical);
    }

    /**
     * Technician workload breakdown for each active technician.
     * Uses a single aggregated database query to avoid the N+1 query pattern.
     */
    public List<TechnicianWorkloadResponse> getTechnicianWorkload() {
        return employeeRepository.findActiveTechnicianWorkloads();
    }

    /**
     * Generates a date-range report with created and resolved ticket counts.
     */
    public DateRangeReportResponse getDateRangeReport(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Both startDate and endDate are required.");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate (" + startDate + ") cannot be after endDate (" + endDate + ").");
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        long createdCount = ticketRepository.countCreatedBetween(startDateTime, endDateTime);
        long resolvedCount = ticketRepository.countResolvedBetween(startDateTime, endDateTime);

        return new DateRangeReportResponse(startDate, endDate, createdCount, resolvedCount);
    }
}
