package com.example.ticket.repository;

import com.example.ticket.entity.Event;
import com.example.ticket.entity.EventStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventStatusHistoryRepository extends JpaRepository<EventStatusHistory, Long> {
    List<EventStatusHistory> findByEventOrderByCreatedAtAsc(Event event);
}