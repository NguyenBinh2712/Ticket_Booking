package com.example.ticket.repository;

import com.example.ticket.entity.Event;
import com.example.ticket.entity.ProducerProfile;
import com.example.ticket.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByProducer(ProducerProfile producer);
    Optional<Event> findByIdAndProducer(Long id, ProducerProfile producer);
    List<Event> findByStatus(EventStatus status);
}