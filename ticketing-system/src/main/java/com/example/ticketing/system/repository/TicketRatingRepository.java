package com.example.ticketing.system.repository;

import com.example.ticketing.system.entity.TicketRating;
import com.example.ticketing.system.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketRatingRepository extends JpaRepository<TicketRating, Long> {
    Optional<TicketRating> findByTicket(Ticket ticket);

    @Query("SELECT AVG(r.rating) FROM TicketRating r")
    Double getAverageRating();
}
