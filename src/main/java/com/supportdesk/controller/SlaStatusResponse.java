package com.supportdesk.controller;

import com.supportdesk.entity.TicketPriority;
import com.supportdesk.entity.TicketStatus;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Response DTO providing detailed SLA deadline and overdue metrics for a ticket.
 */
public class SlaStatusResponse {

    private Long ticketId;
    private String ticketNumber;
    private TicketPriority priority;
    private TicketStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime slaDeadline;
    private boolean overdue;
    private long totalSlaHours;
    private long minutesRemaining;

    public SlaStatusResponse() {
    }

    public SlaStatusResponse(Long ticketId, String ticketNumber, TicketPriority priority,
                             TicketStatus status, LocalDateTime createdAt,
                             LocalDateTime slaDeadline, boolean overdue, long totalSlaHours) {
        this.ticketId = ticketId;
        this.ticketNumber = ticketNumber;
        this.priority = priority;
        this.status = status;
        this.createdAt = createdAt;
        this.slaDeadline = slaDeadline;
        this.overdue = overdue;
        this.totalSlaHours = totalSlaHours;

        if (slaDeadline != null) {
            Duration diff = Duration.between(LocalDateTime.now(), slaDeadline);
            this.minutesRemaining = diff.toMinutes();
        }
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getSlaDeadline() {
        return slaDeadline;
    }

    public void setSlaDeadline(LocalDateTime slaDeadline) {
        this.slaDeadline = slaDeadline;
    }

    public boolean isOverdue() {
        return overdue;
    }

    public void setOverdue(boolean overdue) {
        this.overdue = overdue;
    }

    public long getTotalSlaHours() {
        return totalSlaHours;
    }

    public void setTotalSlaHours(long totalSlaHours) {
        this.totalSlaHours = totalSlaHours;
    }

    public long getMinutesRemaining() {
        return minutesRemaining;
    }

    public void setMinutesRemaining(long minutesRemaining) {
        this.minutesRemaining = minutesRemaining;
    }
}
