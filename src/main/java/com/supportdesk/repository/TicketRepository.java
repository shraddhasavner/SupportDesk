package com.supportdesk.repository;

import com.supportdesk.entity.Ticket;
import com.supportdesk.entity.TicketPriority;
import com.supportdesk.entity.TicketStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * TicketRepository: Data Access Layer for the Ticket entity.
 * 
 * [English]:
 * Extends JpaRepository to provide CRUD operations and derived query methods
 * for filtering tickets by status, priority, creator, and assigned technician.
 * 
 * [Hinglish]:
 * Yeh interface tickets table par CRUD aur filtering operations (status, priority, technician ke basis par) provide karta hai.
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    /**
     * Find a ticket by its unique ticket number (e.g., "TCK-20261014-A1B2C3").
     */
    Optional<Ticket> findByTicketNumber(String ticketNumber);

    /**
     * Find all tickets with a specific status (e.g., OPEN, IN_PROGRESS, RESOLVED).
     */
    List<Ticket> findByStatus(TicketStatus status);

    /**
     * Find all tickets with a specific priority (e.g., HIGH, CRITICAL).
     */
    List<Ticket> findByPriority(TicketPriority priority);

    /**
     * Find tickets filtered by both status and priority.
     */
    List<Ticket> findByStatusAndPriority(TicketStatus status, TicketPriority priority);

    /**
     * Find all tickets created by a specific employee.
     */
    List<Ticket> findByEmployeeId(Long employeeId);

    /**
     * Find tickets created by a specific employee filtered by status.
     */
    List<Ticket> findByEmployeeIdAndStatus(Long employeeId, TicketStatus status);

    /**
     * Find tickets created by a specific employee filtered by priority.
     */
    List<Ticket> findByEmployeeIdAndPriority(Long employeeId, TicketPriority priority);

    /**
     * Find tickets created by a specific employee filtered by status and priority.
     */
    List<Ticket> findByEmployeeIdAndStatusAndPriority(Long employeeId, TicketStatus status, TicketPriority priority);

    /**
     * Find all tickets currently assigned to a specific technician.
     */
    List<Ticket> findByAssignedTechnicianId(Long technicianId);

    /**
     * Check if a ticket number already exists (used during safe ticket number generation).
     */
    boolean existsByTicketNumber(String ticketNumber);

    /**
     * Find all tickets whose SLA deadline is before 'now' and whose status is not RESOLVED or CLOSED.
     */
    @Query("SELECT t FROM Ticket t WHERE t.slaDeadline < :now AND t.status NOT IN (com.supportdesk.entity.TicketStatus.RESOLVED, com.supportdesk.entity.TicketStatus.CLOSED) ORDER BY t.slaDeadline ASC")
    List<Ticket> findOverdueTickets(@Param("now") LocalDateTime now);

    // ==================== Efficient Count Queries for Dashboard ====================

    /**
     * Count tickets by status directly in the database.
     */
    long countByStatus(TicketStatus status);

    /**
     * Count tickets by priority directly in the database.
     */
    long countByPriority(TicketPriority priority);

    /**
     * Count overdue tickets directly in the database (deadline passed and status not RESOLVED/CLOSED).
     */
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.slaDeadline < :now AND t.status NOT IN (com.supportdesk.entity.TicketStatus.RESOLVED, com.supportdesk.entity.TicketStatus.CLOSED)")
    long countOverdueTickets(@Param("now") LocalDateTime now);

    /**
     * Count total tickets assigned to a specific technician.
     */
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.assignedTechnician.id = :techId")
    long countByAssignedTechnicianId(@Param("techId") Long techId);

    /**
     * Count pending tickets (ASSIGNED or IN_PROGRESS) assigned to a specific technician.
     */
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.assignedTechnician.id = :techId AND t.status IN (com.supportdesk.entity.TicketStatus.ASSIGNED, com.supportdesk.entity.TicketStatus.IN_PROGRESS)")
    long countPendingByTechnicianId(@Param("techId") Long techId);

    /**
     * Count resolved tickets (RESOLVED or CLOSED) assigned to a specific technician.
     */
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.assignedTechnician.id = :techId AND t.status IN (com.supportdesk.entity.TicketStatus.RESOLVED, com.supportdesk.entity.TicketStatus.CLOSED)")
    long countResolvedByTechnicianId(@Param("techId") Long techId);

    /**
     * Count tickets created between two dates/times.
     */
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.createdAt >= :start AND t.createdAt <= :end")
    long countCreatedBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Count tickets resolved/closed between two dates/times.
     */
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status IN (com.supportdesk.entity.TicketStatus.RESOLVED, com.supportdesk.entity.TicketStatus.CLOSED) AND t.updatedAt >= :start AND t.updatedAt <= :end")
    long countResolvedBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
