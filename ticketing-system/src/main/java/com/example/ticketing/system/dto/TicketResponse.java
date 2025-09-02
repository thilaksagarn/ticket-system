package com.example.ticketing.system.dto;

import com.example.ticketing.system.entity.Ticket;
import java.time.LocalDateTime;
import java.util.List;

public class TicketResponse {
    private Long id;
    private String subject;
    private String description;
    private String priority;
    private String status;
    private UserResponse createdBy;
    private UserResponse assignedTo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;
    private List<CommentResponse> comments;
    private Integer rating;
    private String ratingFeedback;

    // Constructor from Entity
    public TicketResponse(Ticket ticket) {
        this.id = ticket.getId();
        this.subject = ticket.getSubject();
        this.description = ticket.getDescription();
        this.priority = ticket.getPriority().name();
        this.status = ticket.getStatus().name();
        this.createdBy = new UserResponse(ticket.getCreatedBy());
        this.assignedTo = ticket.getAssignedTo() != null ? new UserResponse(ticket.getAssignedTo()) : null;
        this.createdAt = ticket.getCreatedAt();
        this.updatedAt = ticket.getUpdatedAt();
        this.resolvedAt = ticket.getResolvedAt();
        this.closedAt = ticket.getClosedAt();
        if (ticket.getRating() != null) {
            this.rating = ticket.getRating().getRating();
            this.ratingFeedback = ticket.getRating().getFeedback();
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public UserResponse getCreatedBy() { return createdBy; }
    public void setCreatedBy(UserResponse createdBy) { this.createdBy = createdBy; }

    public UserResponse getAssignedTo() { return assignedTo; }
    public void setAssignedTo(UserResponse assignedTo) { this.assignedTo = assignedTo; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }

    public List<CommentResponse> getComments() { return comments; }
    public void setComments(List<CommentResponse> comments) { this.comments = comments; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getRatingFeedback() { return ratingFeedback; }
    public void setRatingFeedback(String ratingFeedback) { this.ratingFeedback = ratingFeedback; }
}
