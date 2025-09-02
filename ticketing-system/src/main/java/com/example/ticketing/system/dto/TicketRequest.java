package com.example.ticketing.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TicketRequest {
    @NotBlank
    @Size(max = 200)
    private String subject;

    @NotBlank
    private String description;

    private String priority = "MEDIUM";

    // Constructors
    public TicketRequest() {}

    public TicketRequest(String subject, String description, String priority) {
        this.subject = subject;
        this.description = description;
        this.priority = priority;
    }

    // Getters and Setters
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}
