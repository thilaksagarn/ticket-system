package com.example.ticketing.system.repository;

import com.example.ticketing.system.entity.Comment;
import com.example.ticketing.system.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTicketOrderByCreatedAtAsc(Ticket ticket);
    long countByTicket(Ticket ticket);
}
