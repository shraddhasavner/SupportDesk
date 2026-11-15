package com.supportdesk.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Notification Entity: Database-backed in-app user notifications.
 * Maps to the 'notifications' table in MySQL.
 * 
 * [English]:
 * - recipient: The employee receiving this notification (cannot be null).
 * - ticket: The related support ticket (nullable for general notifications).
 * - title: Short summary header (e.g. "Technician Assigned").
 * - message: Detailed human-readable notification text.
 * - type: NotificationType classification (TECHNICIAN_ASSIGNED, STATUS_CHANGED, COMMENT_ADDED, TICKET_RESOLVED).
 * - read: Boolean status indicating whether user has marked it as read.
 * - createdAt: Exact timestamp when notification was produced.
 * 
 * [Hinglish]:
 * - Database-backed notification entity jo user ko system events ki jaankari deti hai.
 * - Jab ticket par koi action hota hai (jaise assign, status change, comment, resolve), tab recipient ke liye banti hai.
 */
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Recipient is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    @JsonIgnore
    private Employee recipient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    @JsonIgnore
    private Ticket ticket;

    @NotBlank(message = "Title is required")
    @Column(nullable = false, length = 150)
    private String title;

    @NotBlank(message = "Message is required")
    @Column(nullable = false, length = 500)
    private String message;

    @NotNull(message = "Notification type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    private NotificationType type;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Default constructor required by JPA.
     */
    public Notification() {
    }

    /**
     * Parameterized constructor for convenient notification creation.
     */
    public Notification(Employee recipient, Ticket ticket, String title, String message, NotificationType type) {
        this.recipient = recipient;
        this.ticket = ticket;
        this.title = title;
        this.message = message;
        this.type = type;
        this.read = false;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    // ==================== Getters & Setters ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Employee getRecipient() {
        return recipient;
    }

    public void setRecipient(Employee recipient) {
        this.recipient = recipient;
    }

    @JsonProperty("recipientId")
    public Long getRecipientId() {
        return recipient != null ? recipient.getId() : null;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    @JsonProperty("ticketId")
    public Long getTicketId() {
        return ticket != null ? ticket.getId() : null;
    }

    @JsonProperty("ticketNumber")
    public String getTicketNumber() {
        return ticket != null ? ticket.getTicketNumber() : null;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    @JsonProperty("read")
    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", recipientId=" + (recipient != null ? recipient.getId() : null) +
                ", ticketId=" + (ticket != null ? ticket.getId() : null) +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", read=" + read +
                ", createdAt=" + createdAt +
                '}';
    }
}
