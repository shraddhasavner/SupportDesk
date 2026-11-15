package com.supportdesk.controller;

import java.time.LocalDate;

/**
 * DTO providing ticket volume and resolution statistics within a custom date range.
 */
public class DateRangeReportResponse {

    private LocalDate startDate;
    private LocalDate endDate;
    private long totalCreated;
    private long totalResolved;

    public DateRangeReportResponse() {
    }

    public DateRangeReportResponse(LocalDate startDate, LocalDate endDate, long totalCreated, long totalResolved) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalCreated = totalCreated;
        this.totalResolved = totalResolved;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public long getTotalCreated() {
        return totalCreated;
    }

    public void setTotalCreated(long totalCreated) {
        this.totalCreated = totalCreated;
    }

    public long getTotalResolved() {
        return totalResolved;
    }

    public void setTotalResolved(long totalResolved) {
        this.totalResolved = totalResolved;
    }
}
