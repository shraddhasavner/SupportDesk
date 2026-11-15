package com.supportdesk.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * TicketComment Entity: Maps to the 'ticket_comments' table in MySQL.
 * 
 * [English]:
 * Represents a comment/note added to an IT support ticket.
 * - @ManyToOne with Ticket: Links this comment to a specific ticket.
 * - @ManyToOne with Employee (author): Identifies who posted the comment.
 * - Employee password is kept secure by the Employee entity's WRITE_ONLY property.
 * - Provides commentText and createdAt timestamp.
 * 
 * [Hinglish]:
 * Yeh class ticket ke comments ko represent karti hai.
 * - `ticket`: ManyToOne relation jo batata hai comment kis ticket par kiya gaya hai.
 * - `author`: ManyToOne relation jo batata hai comment kis employee/technician ne likha hai.
 * - Author employee ka password kabhi API response me leak nahi hota.
 */
@Entity
@Table(name = "ticket_comments")
public class TicketComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Ticket is required")
    @ManyToOne(optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @NotNull(message = "Author is required")
    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee author;

    @NotBlank(message = "Comment text is required")
    @Column(name = "comment_text", nullable = false, columnDefinition = "TEXT")
    private String commentText;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Default constructor required by JPA.
     */
    public TicketComment() {
    }

    /**
     * Parameterized constructor for convenient comment creation.
     */
    public TicketComment(Ticket ticket, Employee author, String commentText) {
        this.ticket = ticket;
        this.author = author;
        this.commentText = commentText;
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

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    /**
     * Convenience helper for clients to get ticket ID directly.
     */
    @JsonProperty("ticketId")
    public Long getTicketId() {
        return ticket != null ? ticket.getId() : null;
    }

    public Employee getAuthor() {
        return author;
    }

    public void setAuthor(Employee author) {
        this.author = author;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    /**
     * Alias setter for flexibility with different payload field names.
     */
    public void setComment(String comment) {
        this.commentText = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "TicketComment{" +
                "id=" + id +
                ", ticketId=" + (ticket != null ? ticket.getId() : null) +
                ", author=" + (author != null ? author.getName() : null) +
                ", commentText='" + commentText + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
