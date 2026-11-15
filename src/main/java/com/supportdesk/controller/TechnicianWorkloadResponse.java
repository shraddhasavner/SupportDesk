package com.supportdesk.controller;

/**
 * DTO providing individual workload metrics for an active IT technician.
 */
public class TechnicianWorkloadResponse {

    private Long technicianId;
    private String technicianName;
    private String department;
    private long assignedTicketCount;
    private long pendingTicketCount;
    private long resolvedTicketCount;

    public TechnicianWorkloadResponse() {
    }

    public TechnicianWorkloadResponse(Long technicianId, String technicianName, String department,
                                      long assignedTicketCount, long pendingTicketCount, long resolvedTicketCount) {
        this.technicianId = technicianId;
        this.technicianName = technicianName;
        this.department = department;
        this.assignedTicketCount = assignedTicketCount;
        this.pendingTicketCount = pendingTicketCount;
        this.resolvedTicketCount = resolvedTicketCount;
    }

    public Long getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(Long technicianId) {
        this.technicianId = technicianId;
    }

    public String getTechnicianName() {
        return technicianName;
    }

    public void setTechnicianName(String technicianName) {
        this.technicianName = technicianName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public long getAssignedTicketCount() {
        return assignedTicketCount;
    }

    public void setAssignedTicketCount(long assignedTicketCount) {
        this.assignedTicketCount = assignedTicketCount;
    }

    public long getPendingTicketCount() {
        return pendingTicketCount;
    }

    public void setPendingTicketCount(long pendingTicketCount) {
        this.pendingTicketCount = pendingTicketCount;
    }

    public long getResolvedTicketCount() {
        return resolvedTicketCount;
    }

    public void setResolvedTicketCount(long resolvedTicketCount) {
        this.resolvedTicketCount = resolvedTicketCount;
    }
}
