package com.example.ticketing.system.controller;

import com.example.ticketing.system.entity.Role;
import com.example.ticketing.system.entity.TicketStatus;
import com.example.ticketing.system.entity.User;
import com.example.ticketing.system.dto.UserResponse;
import com.example.ticketing.system.service.TicketService;
import com.example.ticketing.system.service.UserService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private TicketService ticketService;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserResponse> response = users.stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/tickets/{id}/assign/{userId}")
    public ResponseEntity<?> forceAssignTicket(@PathVariable Long id, @PathVariable Long userId) {
        try {
            User assignee = userService.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Get current user for audit trail
            User currentUser = userService.getUserById(1L).orElse(null); // This should be from auth context

            ticketService.assignTicket(id, assignee, currentUser);
            return ResponseEntity.ok("Ticket assigned successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error assigning ticket: " + e.getMessage());
        }
    }

    @PutMapping("/tickets/{id}/status/{status}")
    public ResponseEntity<?> forceUpdateTicketStatus(@PathVariable Long id, @PathVariable String status) {
        try {
            TicketStatus ticketStatus = TicketStatus.valueOf(status.toUpperCase());
            User currentUser = userService.getUserById(1L).orElse(null); // This should be from auth context

            ticketService.updateTicketStatus(id, ticketStatus, currentUser);
            return ResponseEntity.ok("Ticket status updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating ticket status: " + e.getMessage());
        }
    }

    @DeleteMapping("/tickets/{id}")
    public ResponseEntity<?> deleteTicket(@PathVariable Long id) {
        try {
            ticketService.deleteTicket(id);
            return ResponseEntity.ok("Ticket deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting ticket: " + e.getMessage());
        }
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalTickets", ticketService.getTotalTickets());
        stats.put("openTickets", ticketService.getOpenTickets());
        stats.put("inProgressTickets", ticketService.getInProgressTickets());
        stats.put("resolvedTickets", ticketService.getResolvedTickets());
        stats.put("closedTickets", ticketService.getClosedTickets());

        stats.put("totalUsers", userService.getAllUsers().size());
        stats.put("enabledUsers", userService.getEnabledUsers().size());
        stats.put("supportAgents", userService.getUsersByRole(Role.SUPPORT_AGENT).size());
        stats.put("admins", userService.getUsersByRole(Role.ADMIN).size());

        return ResponseEntity.ok(stats);
    }
}
