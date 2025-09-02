package com.example.ticketing.system.service;

import com.example.ticketing.system.entity.*;
import com.example.ticketing.system.repository.TicketRepository;
import com.example.ticketing.system.repository.CommentRepository;
import com.example.ticketing.system.repository.TicketRatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TicketRatingRepository ratingRepository;

    @Autowired
    private EmailService emailService;

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public Page<Ticket> getAllTickets(Pageable pageable) {
        return ticketRepository.findAll(pageable);
    }

    public Optional<Ticket> getTicketById(Long id) {
        return ticketRepository.findById(id);
    }

    public List<Ticket> getTicketsByUser(User user) {
        return ticketRepository.findByCreatedByOrderByCreatedAtDesc(user);
    }

    public List<Ticket> getTicketsAssignedTo(User user) {
        return ticketRepository.findByAssignedToOrderByCreatedAtDesc(user);
    }

    public List<Ticket> getTicketsByStatus(TicketStatus status) {
        return ticketRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public List<Ticket> searchTickets(String searchTerm) {
        return ticketRepository.searchTickets(searchTerm);
    }

    public List<Ticket> searchUserTickets(User user, String searchTerm) {
        return ticketRepository.searchUserTickets(user, searchTerm);
    }

    public Ticket createTicket(String subject, String description, Priority priority, User createdBy, MultipartFile file) {
        Ticket ticket = new Ticket(subject, description, priority, createdBy);
        Ticket savedTicket = ticketRepository.save(ticket);

        // Send email notification
        emailService.sendTicketCreatedNotification(savedTicket);

        return savedTicket;
    }

    public Ticket updateTicketStatus(Long ticketId, TicketStatus status, User updatedBy) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        TicketStatus oldStatus = ticket.getStatus();
        ticket.setStatus(status);
        Ticket savedTicket = ticketRepository.save(ticket);

        // Send email notification if status changed
        if (oldStatus != status) {
            emailService.sendTicketStatusUpdateNotification(savedTicket, oldStatus, status);
        }

        return savedTicket;
    }

    public Ticket assignTicket(Long ticketId, User assignedTo, User assignedBy) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        User oldAssignee = ticket.getAssignedTo();
        ticket.setAssignedTo(assignedTo);
        if (ticket.getStatus() == TicketStatus.OPEN) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        }

        Ticket savedTicket = ticketRepository.save(ticket);

        // Send email notifications
        emailService.sendTicketAssignmentNotification(savedTicket, oldAssignee);

        return savedTicket;
    }

    public Ticket updateTicket(Long ticketId, String subject, String description, Priority priority) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (subject != null) ticket.setSubject(subject);
        if (description != null) ticket.setDescription(description);
        if (priority != null) ticket.setPriority(priority);

        return ticketRepository.save(ticket);
    }

    public Comment addComment(Long ticketId, String content, User user) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        Comment comment = new Comment(content, ticket, user);
        Comment savedComment = commentRepository.save(comment);

        // Send email notification
        emailService.sendNewCommentNotification(ticket, savedComment);

        return savedComment;
    }

    public List<Comment> getTicketComments(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        return commentRepository.findByTicketOrderByCreatedAtAsc(ticket);
    }

    public TicketRating rateTicket(Long ticketId, Integer rating, String feedback) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (ticket.getStatus() != TicketStatus.RESOLVED && ticket.getStatus() != TicketStatus.CLOSED) {
            throw new RuntimeException("Can only rate resolved or closed tickets");
        }

        Optional<TicketRating> existingRating = ratingRepository.findByTicket(ticket);
        TicketRating ticketRating;

        if (existingRating.isPresent()) {
            ticketRating = existingRating.get();
            ticketRating.setRating(rating);
            ticketRating.setFeedback(feedback);
        } else {
            ticketRating = new TicketRating(ticket, rating, feedback);
        }

        return ratingRepository.save(ticketRating);
    }

    public void deleteTicket(Long ticketId) {
        ticketRepository.deleteById(ticketId);
    }

    // Dashboard statistics
    public long getTotalTickets() {
        return ticketRepository.count();
    }

    public long getOpenTickets() {
        return ticketRepository.countByStatus(TicketStatus.OPEN);
    }

    public long getInProgressTickets() {
        return ticketRepository.countByStatus(TicketStatus.IN_PROGRESS);
    }

    public long getResolvedTickets() {
        return ticketRepository.countByStatus(TicketStatus.RESOLVED);
    }

    public long getClosedTickets() {
        return ticketRepository.countByStatus(TicketStatus.CLOSED);
    }

    public long getUserTicketCount(User user) {
        return ticketRepository.countByCreatedBy(user);
    }

    public long getAssignedTicketCount(User user) {
        return ticketRepository.countByAssignedTo(user);
    }
}