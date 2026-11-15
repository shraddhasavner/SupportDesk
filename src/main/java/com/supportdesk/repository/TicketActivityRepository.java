package com.supportdesk.repository;

import com.supportdesk.entity.TicketActivity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * TicketActivityRepository: Spring Data JPA repository for TicketActivity audit trail.
 * 
 * [English]:
 * Manages persisting and querying activity log entries for IT support tickets.
 * Orders activity history chronologically (oldest first).
 * 
 * [Hinglish]:
 * Ticket par hue activities ka audit log save aur fetch karne ke liye repository.
 * Timeline ko chronological order (sabse pehle se aakhiri tak) me return karta hai.
 */
@Repository
public interface TicketActivityRepository extends JpaRepository<TicketActivity, Long> {

    /**
     * Retrieve all activity records for a given ticket, ordered chronologically (oldest first).
     */
    @Query("SELECT a FROM TicketActivity a WHERE a.ticket.id = :ticketId ORDER BY a.createdAt ASC, a.id ASC")
    List<TicketActivity> findByTicketIdOrderByCreatedAtAscIdAsc(@Param("ticketId") Long ticketId);
}
