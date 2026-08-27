package com.example.ticket.repository;

import com.example.ticket.document.EventDetail;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface EventDetailRepository extends MongoRepository<EventDetail, String> {
    Optional<EventDetail> findByEventId(Long eventId);
}