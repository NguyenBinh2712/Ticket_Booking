package com.example.ticket.repository;

import com.example.ticket.entity.*;
import com.example.ticket.enums.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventVenueContractRepository extends JpaRepository<EventVenueContract, Long> {
    List<EventVenueContract> findByEvent(Event event);
    List<EventVenueContract> findByVenueAndStatus(VenueProfile venue, ContractStatus status);
    Optional<EventVenueContract> findByIdAndVenue(Long id, VenueProfile venue);
}