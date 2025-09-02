package com.example.ticketing.system.repository;

import com.example.ticketing.system.entity.Ticket;
import com.example.ticketing.system.entity.TicketStatus;
import com.example.ticketing.system.entity.Priority;
import com.example.ticketing.system.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByCreatedByOrderByCreatedAtDesc(User createdBy);
    List<Ticket> findByAssignedToOrderByCreatedAtDesc(User assignedTo);
    List<Ticket> findByStatusOrderByCreatedAtDesc(TicketStatus status);
    List<Ticket> findByPriorityOrderByCreatedAtDesc(Priority priority);

    Page<Ticket> findByCreatedBy(User createdBy, Pageable pageable);
    Page<Ticket> findByAssignedTo(User assignedTo, Pageable pageable);
    Page<Ticket> findByStatus(TicketStatus status, Pageable pageable);
    Page<Ticket> findByPriority(Priority priority, Pageable pageable);

    @Query("SELECT t FROM Ticket t WHERE " +
            "LOWER(t.subject) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Ticket> searchTickets(@Param("searchTerm") String searchTerm);

    @Query("SELECT t FROM Ticket t WHERE t.createdBy = :user AND " +
            "(LOWER(t.subject) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Ticket> searchUserTickets(@Param("user") User user, @Param("searchTerm") String searchTerm);

    long countByStatus(TicketStatus status);
    long countByCreatedBy(User createdBy);
    long countByAssignedTo(User assignedTo);
}
