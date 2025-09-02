package com.example.ticketing.system.service;

import com.example.ticketing.system.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender emailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendTicketCreatedNotification(Ticket ticket) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(ticket.getCreatedBy().getEmail());
            message.setSubject("Ticket Created - #" + ticket.getId());
            message.setText(String.format(
                    "Dear %s,\n\n" +
                            "Your ticket has been created successfully.\n\n" +
                            "Ticket ID: #%d\n" +
                            "Subject: %s\n" +
                            "Priority: %s\n" +
                            "Status: %s\n\n" +
                            "We will review your ticket and get back to you soon.\n\n" +
                            "Best regards,\n" +
                            "Support Team",
                    ticket.getCreatedBy().getFirstName(),
                    ticket.getId(),
                    ticket.getSubject(),
                    ticket.getPriority(),
                    ticket.getStatus()
            ));

            emailSender.send(message);
        } catch (Exception e) {
            // Log error but don't fail the operation
            System.err.println("Failed to send email notification: " + e.getMessage());
        }
    }

    public void sendTicketAssignmentNotification(Ticket ticket, User oldAssignee) {
        if (ticket.getAssignedTo() != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(ticket.getAssignedTo().getEmail());
                message.setSubject("Ticket Assigned - #" + ticket.getId());
                message.setText(String.format(
                        "Dear %s,\n\n" +
                                "A ticket has been assigned to you.\n\n" +
                                "Ticket ID: #%d\n" +
                                "Subject: %s\n" +
                                "Priority: %s\n" +
                                "Created by: %s %s\n\n" +
                                "Please review and take appropriate action.\n\n" +
                                "Best regards,\n" +
                                "Support Team",
                        ticket.getAssignedTo().getFirstName(),
                        ticket.getId(),
                        ticket.getSubject(),
                        ticket.getPriority(),
                        ticket.getCreatedBy().getFirstName(),
                        ticket.getCreatedBy().getLastName()
                ));

                emailSender.send(message);
            } catch (Exception e) {
                System.err.println("Failed to send assignment notification: " + e.getMessage());
            }
        }
    }

    public void sendTicketStatusUpdateNotification(Ticket ticket, TicketStatus oldStatus, TicketStatus newStatus) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(ticket.getCreatedBy().getEmail());
            message.setSubject("Ticket Status Updated - #" + ticket.getId());
            message.setText(String.format(
                    "Dear %s,\n\n" +
                            "Your ticket status has been updated.\n\n" +
                            "Ticket ID: #%d\n" +
                            "Subject: %s\n" +
                            "Previous Status: %s\n" +
                            "New Status: %s\n" +
                            "Assigned to: %s\n\n" +
                            "Best regards,\n" +
                            "Support Team",
                    ticket.getCreatedBy().getFirstName(),
                    ticket.getId(),
                    ticket.getSubject(),
                    oldStatus,
                    newStatus,
                    ticket.getAssignedTo() != null ?
                            ticket.getAssignedTo().getFirstName() + " " + ticket.getAssignedTo().getLastName() :
                            "Unassigned"
            ));

            emailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send status update notification: " + e.getMessage());
        }
    }

    public void sendNewCommentNotification(Ticket ticket, Comment comment) {
        try {
            // Notify ticket creator if comment is not from them
            if (!comment.getUser().getId().equals(ticket.getCreatedBy().getId())) {
                SimpleMailMessage creatorMessage = new SimpleMailMessage();
                creatorMessage.setFrom(fromEmail);
                creatorMessage.setTo(ticket.getCreatedBy().getEmail());
                creatorMessage.setSubject("New Comment on Ticket #" + ticket.getId());
                creatorMessage.setText(String.format(
                        "Dear %s,\n\n" +
                                "A new comment has been added to your ticket.\n\n" +
                                "Ticket ID: #%d\n" +
                                "Subject: %s\n" +
                                "Comment by: %s %s\n" +
                                "Comment: %s\n\n" +
                                "Best regards,\n" +
                                "Support Team",
                        ticket.getCreatedBy().getFirstName(),
                        ticket.getId(),
                        ticket.getSubject(),
                        comment.getUser().getFirstName(),
                        comment.getUser().getLastName(),
                        comment.getContent()
                ));

                emailSender.send(creatorMessage);
            }

            // Notify assigned agent if comment is not from them and they're not the creator
            if (ticket.getAssignedTo() != null &&
                    !comment.getUser().getId().equals(ticket.getAssignedTo().getId()) &&
                    !ticket.getAssignedTo().getId().equals(ticket.getCreatedBy().getId())) {

                SimpleMailMessage agentMessage = new SimpleMailMessage();
                agentMessage.setFrom(fromEmail);
                agentMessage.setTo(ticket.getAssignedTo().getEmail());
                agentMessage.setSubject("New Comment on Assigned Ticket #" + ticket.getId());
                agentMessage.setText(String.format(
                        "Dear %s,\n\n" +
                                "A new comment has been added to a ticket assigned to you.\n\n" +
                                "Ticket ID: #%d\n" +
                                "Subject: %s\n" +
                                "Comment by: %s %s\n" +
                                "Comment: %s\n\n" +
                                "Best regards,\n" +
                                "Support Team",
                        ticket.getAssignedTo().getFirstName(),
                        ticket.getId(),
                        ticket.getSubject(),
                        comment.getUser().getFirstName(),
                        comment.getUser().getLastName(),
                        comment.getContent()
                ));

                emailSender.send(agentMessage);
            }
        } catch (Exception e) {
            System.err.println("Failed to send comment notification: " + e.getMessage());
        }
    }
}
