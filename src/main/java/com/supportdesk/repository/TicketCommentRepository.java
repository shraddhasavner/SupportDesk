package com.supportdesk.repository;

import com.supportdesk.entity.TicketComment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * TicketCommentRepository: Spring Data JPA repository for TicketComment entity.
 * 
 * [English]:
 * Provides standard CRUD operations and custom query methods to query comments by ticket ID.
 * Orders comments chronologically (oldest first).
 * 
 * [Hinglish]:
 * Ticket ke comments ko database se fetch aur save karne ke liye repository.
 * Comments ko unke banne ke time ke hisab se line se (oldest first) nikalta hai.
 */
@Repository
public interface TicketCommentRepository extends JpaRepository<TicketComment, Long> {

    /**
     * Retrieve all comments for a given ticket, ordered chronologically (oldest first).
     */
    @Query("SELECT c FROM TicketComment c WHERE c.ticket.id = :ticketId ORDER BY c.createdAt ASC, c.id ASC")
    List<TicketComment> findByTicketIdOrderByCreatedAtAscIdAsc(@Param("ticketId") Long ticketId);
}
