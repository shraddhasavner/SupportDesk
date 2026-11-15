package com.supportdesk.controller;

/**
 * DTO providing overall dashboard statistics on ticket counts and statuses.
 */
public class DashboardSummaryResponse {

    private long totalTickets;
    private long openTickets;
    private long assignedTickets;
    private long inProgressTickets;
    private long resolvedTickets;
    private long closedTickets;
    private long overdueTickets;

    public DashboardSummaryResponse() {
    }

    public DashboardSummaryResponse(long totalTickets, long openTickets, long assignedTickets,
                                    long inProgressTickets, long resolvedTickets,
                                    long closedTickets, long overdueTickets) {
        this.totalTickets = totalTickets;
        this.openTickets = openTickets;
        this.assignedTickets = assignedTickets;
        this.inProgressTickets = inProgressTickets;
        this.resolvedTickets = resolvedTickets;
        this.closedTickets = closedTickets;
        this.overdueTickets = overdueTickets;
    }

    public long getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(long totalTickets) {
        this.totalTickets = totalTickets;
    }

    public long getOpenTickets() {
        return openTickets;
    }

    public void setOpenTickets(long openTickets) {
        this.openTickets = openTickets;
    }

    public long getAssignedTickets() {
        return assignedTickets;
    }

    public void setAssignedTickets(long assignedTickets) {
        this.assignedTickets = assignedTickets;
    }

    public long getInProgressTickets() {
        return inProgressTickets;
    }

    public void setInProgressTickets(long inProgressTickets) {
        this.inProgressTickets = inProgressTickets;
    }

    public long getResolvedTickets() {
        return resolvedTickets;
    }

    public void setResolvedTickets(long resolvedTickets) {
        this.resolvedTickets = resolvedTickets;
    }

    public long getClosedTickets() {
        return closedTickets;
    }

    public void setClosedTickets(long closedTickets) {
        this.closedTickets = closedTickets;
    }

    public long getOverdueTickets() {
        return overdueTickets;
    }

    public void setOverdueTickets(long overdueTickets) {
        this.overdueTickets = overdueTickets;
    }
}
