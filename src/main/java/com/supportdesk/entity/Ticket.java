package com.supportdesk.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Ticket Entity: Maps directly to the 'tickets' table in MySQL.
 * 
 * [English]:
 * Represents an IT support request.
 * - @ManyToOne with Employee (creator): Identifies the staff member who reported the issue.
 * - @ManyToOne with Employee (assignedTechnician): Identifies the IT specialist resolving it.
 * - @Enumerated(EnumType.STRING): Saves priority and status as text in the database.
 * 
 * [Hinglish]:
 * Yeh class hamari 'tickets' table ka model hai.
 * - `employee`: ManyToOne relation jo batata hai ticket kisne banaya.
 * - `assignedTechnician`: ManyToOne relation jo batata hai ticket kis technician ko assign hua.
 */
@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_number", nullable = false, unique = true, length = 50)
    private String ticketNumber;

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    @Column(nullable = false, length = 150)
    private String title;

    @NotBlank(message = "Description is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "Category is required")
    @Size(max = 50, message = "Category must not exceed 50 characters")
    @Column(nullable = false, length = 50)
    private String category;

    @NotNull(message = "Priority is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketPriority priority = TicketPriority.MEDIUM;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status = TicketStatus.OPEN;

    /**
     * The employee who created the ticket.
     * Cannot be null.
     */
    @NotNull(message = "Creator employee is required")
    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /**
     * The IT technician assigned to solve the ticket.
     * Null when ticket is newly opened and unassigned.
     */
    @ManyToOne
    @JoinColumn(name = "assigned_technician_id")
    private Employee assignedTechnician;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "sla_deadline")
    private LocalDateTime slaDeadline;

    /**
     * Default constructor required by JPA/Hibernate.
     */
    public Ticket() {
    }

    /**
     * Parameterized constructor for convenient ticket creation.
     */
    public Ticket(String ticketNumber, String title, String description, String category,
                  TicketPriority priority, Employee employee) {
        this.ticketNumber = ticketNumber;
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority != null ? priority : TicketPriority.MEDIUM;
        this.status = TicketStatus.OPEN;
        this.employee = employee;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = TicketStatus.OPEN;
        }
        if (this.priority == null) {
            this.priority = TicketPriority.MEDIUM;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ==================== Getters & Setters ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Employee getAssignedTechnician() {
        return assignedTechnician;
    }

    public void setAssignedTechnician(Employee assignedTechnician) {
        this.assignedTechnician = assignedTechnician;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    public LocalDateTime getSlaDeadline() {
        return slaDeadline;
    }

    public void setSlaDeadline(LocalDateTime slaDeadline) {
        this.slaDeadline = slaDeadline;
    }

    /**
     * Determines whether the ticket is currently overdue.
     * 
     * [English]:
     * Overdue condition:
     * 1. The ticket has an SLA deadline.
     * 2. The current time is strictly after the deadline.
     * 3. The ticket status is NOT RESOLVED and NOT CLOSED.
     * 
     * [Hinglish]:
     * Ticket tabhi overdue mana jata hai jab deadline nikal chuki ho
     * AUR ticket RESOLVED ya CLOSED na ho.
     */
    @Transient
    @JsonProperty("overdue")
    public boolean isOverdue() {
        if (slaDeadline == null) {
            return false;
        }
        if (status == TicketStatus.RESOLVED || status == TicketStatus.CLOSED) {
            return false;
        }
        return LocalDateTime.now().isAfter(slaDeadline);
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "id=" + id +
                ", ticketNumber='" + ticketNumber + '\'' +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", priority=" + priority +
                ", status=" + status +
                ", employee=" + (employee != null ? employee.getName() : "null") +
                ", assignedTechnician=" + (assignedTechnician != null ? assignedTechnician.getName() : "unassigned") +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
