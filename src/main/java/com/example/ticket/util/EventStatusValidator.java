package com.example.ticket.util;

import com.example.ticket.enums.EventStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class EventStatusValidator {

    private final Map<EventStatus, Set<EventStatus>> allowedTransitions = new EnumMap<>(EventStatus.class);

    public EventStatusValidator() {
        allowedTransitions.put(EventStatus.DRAFT, EnumSet.of(EventStatus.SUBMITTED, EventStatus.CANCELLED));
        allowedTransitions.put(EventStatus.SUBMITTED, EnumSet.of(EventStatus.ADMIN_REVIEWING, EventStatus.CANCELLED));
        allowedTransitions.put(EventStatus.ADMIN_REVIEWING, EnumSet.of(EventStatus.MATCHING, EventStatus.CANCELLED));
        allowedTransitions.put(EventStatus.MATCHING, EnumSet.of(EventStatus.PENDING_VENUE_APPROVAL, EventStatus.CANCELLED));
        allowedTransitions.put(EventStatus.PENDING_VENUE_APPROVAL, EnumSet.of(EventStatus.VENUE_ACCEPTED, EventStatus.VENUE_REJECTED));
        allowedTransitions.put(EventStatus.VENUE_ACCEPTED, EnumSet.of(EventStatus.CONTRACT_CONFIRMED, EventStatus.CANCELLED));
        allowedTransitions.put(EventStatus.VENUE_REJECTED, EnumSet.of(EventStatus.MATCHING, EventStatus.CANCELLED));
        allowedTransitions.put(EventStatus.CONTRACT_CONFIRMED, EnumSet.of(EventStatus.PUBLISHED, EventStatus.CANCELLED));
        allowedTransitions.put(EventStatus.PUBLISHED, EnumSet.of(EventStatus.ONGOING, EventStatus.CANCELLED));
        allowedTransitions.put(EventStatus.ONGOING, EnumSet.of(EventStatus.COMPLETED));
        allowedTransitions.put(EventStatus.COMPLETED, EnumSet.of(EventStatus.SETTLED));
        allowedTransitions.put(EventStatus.SETTLED, EnumSet.noneOf(EventStatus.class));
        allowedTransitions.put(EventStatus.CANCELLED, EnumSet.noneOf(EventStatus.class));
    }

    public boolean isValidTransition(EventStatus from, EventStatus to) {
        return allowedTransitions.getOrDefault(from, EnumSet.noneOf(EventStatus.class)).contains(to);
    }
}