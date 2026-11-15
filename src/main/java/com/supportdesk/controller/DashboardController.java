package com.supportdesk.controller;

import com.supportdesk.service.DashboardService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * DashboardController: REST endpoints for help desk metrics and reports.
 * 
 * [English]:
 * - Base path: /api/dashboard
 * - Provides summary statistics, priority breakdown, technician workload, and date-range reports.
 * 
 * [Hinglish]:
 * - Dashboard ke saare analytics aur reports is controller ke endpoints se access hote hain.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Overall ticket counts across all statuses and overdue metric.
     * GET /api/dashboard/summary
     */
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary() {
        DashboardSummaryResponse summary = dashboardService.getDashboardSummary();
        return ResponseEntity.ok(summary);
    }

    /**
     * Ticket counts breakdown by priority (LOW, MEDIUM, HIGH, CRITICAL).
     * GET /api/dashboard/priority-summary
     */
    @GetMapping("/priority-summary")
    public ResponseEntity<PrioritySummaryResponse> getPrioritySummary() {
        PrioritySummaryResponse prioritySummary = dashboardService.getPrioritySummary();
        return ResponseEntity.ok(prioritySummary);
    }

    /**
     * Active technician workload breakdown (assigned, pending, resolved).
     * GET /api/dashboard/technician-workload
     */
    @GetMapping("/technician-workload")
    public ResponseEntity<List<TechnicianWorkloadResponse>> getTechnicianWorkload() {
        List<TechnicianWorkloadResponse> workload = dashboardService.getTechnicianWorkload();
        return ResponseEntity.ok(workload);
    }

    /**
     * Date-range report for created and resolved ticket counts.
     * GET /api/dashboard/reports?startDate=2026-10-01&endDate=2026-10-31
     */
    @GetMapping("/reports")
    public ResponseEntity<DateRangeReportResponse> getDateRangeReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        DateRangeReportResponse report = dashboardService.getDateRangeReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }
}
