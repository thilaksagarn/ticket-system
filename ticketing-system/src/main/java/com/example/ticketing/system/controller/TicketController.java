package com.example.ticketing.system.controller;
import com.example.ticketing.system.dto.*;
import com.example.ticketing.system.entity.*;
import com.example.ticketing.system.service.TicketService;
import com.example.ticketing.system.service.FileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private FileService fileService;

    @GetMapping
    public ResponseEntity<List<TicketResponse>> getAllTickets(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        List<Ticket> tickets;

        if (currentUser.getRole() == Role.ADMIN) {
            tickets = ticketService.getAllTickets();
        } else if (currentUser.getRole() == Role.SUPPORT_AGENT) {
            // Support agents see all tickets
            tickets = ticketService.getAllTickets();
        } else {
            // Regular users see only their tickets
            tickets = ticketService.getTicketsByUser(currentUser);
        }

        List<TicketResponse> response = tickets.stream()
                .map(ticket -> {
                    TicketResponse ticketResponse = new TicketResponse(ticket);
                    // Load comments
                    List<CommentResponse> comments = ticketService.getTicketComments(ticket.getId())
                            .stream()
                            .map(CommentResponse::new)
                            .collect(Collectors.toList());
                    ticketResponse.setComments(comments);
                    return ticketResponse;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long id, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        Ticket ticket = ticketService.getTicketById(id)
                .orElse(null);

        if (ticket == null) {
            return ResponseEntity.notFound().build();
        }

        // Check access permissions
        if (currentUser.getRole() == Role.USER &&
                !ticket.getCreatedBy().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).build();
        }

        TicketResponse response = new TicketResponse(ticket);
        // Load comments
        List<CommentResponse> comments = ticketService.getTicketComments(ticket.getId())
                .stream()
                .map(CommentResponse::new)
                .collect(Collectors.toList());
        response.setComments(comments);

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/create",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createTicket(
            @RequestParam("subject") String subject,
            @RequestParam("description") String description,
            @RequestParam("priority") String priority,
            @RequestPart(value = "file", required = false) MultipartFile file,
            Authentication authentication
    ) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            Priority priorityEnum = Priority.valueOf(priority.toUpperCase());

            // Pass file to your service if needed
            Ticket ticket = ticketService.createTicket(
                    subject,
                    description,
                    priorityEnum,
                    currentUser,
                    file // Add this parameter to your service method
            );

            return ResponseEntity.ok(new TicketResponse(ticket));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating ticket: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPORT_AGENT')")
    public ResponseEntity<?> updateTicketStatus(@PathVariable Long id,
                                                @RequestBody String status,
                                                Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            TicketStatus ticketStatus = TicketStatus.valueOf(status.replace("\"", "").toUpperCase());

            Ticket ticket = ticketService.updateTicketStatus(id, ticketStatus, currentUser);
            return ResponseEntity.ok(new TicketResponse(ticket));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating ticket status: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPORT_AGENT')")
    public ResponseEntity<?> assignTicket(@PathVariable Long id,
                                          @RequestBody Long assigneeId,
                                          Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            User assignee = ticketService.getTicketById(assigneeId)
                    .map(Ticket::getCreatedBy)
                    .orElse(null);

            if (assignee == null) {
                return ResponseEntity.badRequest().body("Assignee not found");
            }

            Ticket ticket = ticketService.assignTicket(id, assignee, currentUser);
            return ResponseEntity.ok(new TicketResponse(ticket));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error assigning ticket: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<?> addComment(@PathVariable Long id,
                                        @RequestBody String content,
                                        Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            String cleanContent = content.replace("\"", "");

            // Check if user can comment on this ticket
            Ticket ticket = ticketService.getTicketById(id).orElse(null);
            if (ticket == null) {
                return ResponseEntity.notFound().build();
            }

            if (currentUser.getRole() == Role.USER &&
                    !ticket.getCreatedBy().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(403).build();
            }

            Comment comment = ticketService.addComment(id, cleanContent, currentUser);
            return ResponseEntity.ok(new CommentResponse(comment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error adding comment: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/rate")
    public ResponseEntity<?> rateTicket(@PathVariable Long id,
                                        @RequestParam Integer rating,
                                        @RequestParam(required = false) String feedback,
                                        Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();

            // Check if user can rate this ticket
            Ticket ticket = ticketService.getTicketById(id).orElse(null);
            if (ticket == null) {
                return ResponseEntity.notFound().build();
            }

            if (!ticket.getCreatedBy().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(403).build();
            }

            TicketRating ticketRating = ticketService.rateTicket(id, rating, feedback);
            return ResponseEntity.ok("Ticket rated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error rating ticket: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/attachments")
    public ResponseEntity<?> uploadAttachment(@PathVariable Long id,
                                              @RequestParam("file") MultipartFile file,
                                              Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();

            Ticket ticket = ticketService.getTicketById(id).orElse(null);
            if (ticket == null) {
                return ResponseEntity.notFound().build();
            }

            // Check permissions
            if (currentUser.getRole() == Role.USER &&
                    !ticket.getCreatedBy().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(403).build();
            }

            TicketAttachment attachment = fileService.saveFile(file, ticket, currentUser);
            return ResponseEntity.ok("File uploaded successfully");
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Error uploading file: " + e.getMessage());
        }
    }

    @GetMapping("/attachments/{attachmentId}")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable Long attachmentId,
                                                     Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            TicketAttachment attachment = fileService.getAttachment(attachmentId);

            // Check permissions
            Ticket ticket = attachment.getTicket();
            if (currentUser.getRole() == Role.USER &&
                    !ticket.getCreatedBy().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(403).build();
            }

            byte[] fileContent = fileService.getFileContent(attachmentId);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + attachment.getFileName() + "\"")
                    .body(fileContent);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<TicketResponse>> searchTickets(@RequestParam String query,
                                                              Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        List<Ticket> tickets;

        if (currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.SUPPORT_AGENT) {
            tickets = ticketService.searchTickets(query);
        } else {
            tickets = ticketService.searchUserTickets(currentUser, query);
        }

        List<TicketResponse> response = tickets.stream()
                .map(TicketResponse::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-tickets")
    public ResponseEntity<List<TicketResponse>> getMyTickets(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        List<Ticket> tickets = ticketService.getTicketsByUser(currentUser);

        List<TicketResponse> response = tickets.stream()
                .map(TicketResponse::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/assigned-to-me")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPORT_AGENT')")
    public ResponseEntity<List<TicketResponse>> getAssignedTickets(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        List<Ticket> tickets = ticketService.getTicketsAssignedTo(currentUser);

        List<TicketResponse> response = tickets.stream()
                .map(TicketResponse::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}