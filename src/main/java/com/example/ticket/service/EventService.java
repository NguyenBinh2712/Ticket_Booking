package com.example.ticket.service;

import com.example.ticket.document.EventDetail;
import com.example.ticket.dto.event.*;
import com.example.ticket.entity.*;
import com.example.ticket.enums.EventStatus;
import com.example.ticket.enums.ProfileStatus;
import com.example.ticket.exception.AppException;
import com.example.ticket.exception.ErrorCode;
import com.example.ticket.repository.*;
import com.example.ticket.util.EventStatusValidator;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class EventService {

    EventRepository eventRepository;
    EventStatusHistoryRepository eventStatusHistoryRepository;
    EventDetailRepository eventDetailRepository;
    ProducerProfileRepository producerProfileRepository;
    UserRepository userRepository;
    EventStatusValidator eventStatusValidator;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    private ProducerProfile getCurrentVerifiedProducer() {
        User user = getCurrentUser();
        ProducerProfile producer = producerProfileRepository.findByUser(user)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCER_PROFILE_NOT_FOUND));
        if (producer.getStatus() != ProfileStatus.VERIFIED) {
            throw new AppException(ErrorCode.PRODUCER_NOT_VERIFIED);
        }
        return producer;
    }

    public EventResponse createEvent(EventRequest request) {
        ProducerProfile producer = getCurrentVerifiedProducer();

        Event event = Event.builder()
                .producer(producer)
                .title(request.getTitle())
                .type(request.getType())
                .description(request.getDescription())
                .posterUrl(request.getPosterUrl())
                .status(EventStatus.DRAFT)
                .build();
        event = eventRepository.save(event);
        event.setProducerSharePercent(request.getProducerSharePercent());
        eventRepository.save(event);
        EventDetail detail = saveEventDetail(event.getId(), request);
        event.setDetailDocId(detail.getId());
        eventRepository.save(event);
        logStatusChange(event, null, EventStatus.DRAFT, "Tạo sự kiện");
        return toResponse(event, detail);
    }

    public EventResponse updateEvent(Long eventId, EventRequest request) {
        ProducerProfile producer = getCurrentVerifiedProducer();
        Event event = eventRepository.findByIdAndProducer(eventId, producer)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new AppException(ErrorCode.EVENT_NOT_EDITABLE);
        }

        event.setTitle(request.getTitle());
        event.setType(request.getType());
        event.setDescription(request.getDescription());
        event.setPosterUrl(request.getPosterUrl());
        event.setProducerSharePercent(request.getProducerSharePercent());
        eventRepository.save(event);

        EventDetail detail = saveEventDetail(event.getId(), request);

        return toResponse(event, detail);
    }

    public EventResponse submitEvent(Long eventId) {
        ProducerProfile producer = getCurrentVerifiedProducer();
        Event event = eventRepository.findByIdAndProducer(eventId, producer)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        changeStatus(event, EventStatus.SUBMITTED, "Producer gửi duyệt");
        return toResponse(event, getEventDetail(event.getId()));
    }

    public List<EventResponse> getMyEvents() {
        ProducerProfile producer = getCurrentVerifiedProducer();
        return eventRepository.findByProducer(producer).stream()
                .map(e -> toResponse(e, getEventDetail(e.getId())))
                .collect(Collectors.toList());
    }

    public EventResponse getMyEventDetail(Long eventId) {
        ProducerProfile producer = getCurrentVerifiedProducer();
        Event event = eventRepository.findByIdAndProducer(eventId, producer)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        return toResponse(event, getEventDetail(event.getId()));
    }

    public List<EventStatusHistoryResponse> getStatusHistory(Long eventId) {
        ProducerProfile producer = getCurrentVerifiedProducer();
        Event event = eventRepository.findByIdAndProducer(eventId, producer)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        return eventStatusHistoryRepository.findByEventOrderByCreatedAtAsc(event).stream()
                .map(h -> EventStatusHistoryResponse.builder()
                        .fromStatus(h.getFromStatus())
                        .toStatus(h.getToStatus())
                        .changedByEmail(h.getChangedBy() != null ? h.getChangedBy().getEmail() : "system")
                        .note(h.getNote())
                        .createdAt(h.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    // Dùng chung cho các Cụm sau (Admin duyệt ở Cụm 5 sẽ gọi lại hàm này)
    public void changeStatus(Event event, EventStatus newStatus, String note) {
        if (!eventStatusValidator.isValidTransition(event.getStatus(), newStatus)) {
            throw new AppException(ErrorCode.INVALID_EVENT_STATUS_TRANSITION);
        }
        EventStatus oldStatus = event.getStatus();
        event.setStatus(newStatus);
        eventRepository.save(event);
        logStatusChange(event, oldStatus, newStatus, note);
    }

    private void logStatusChange(Event event, EventStatus from, EventStatus to, String note) {
        User currentUser;
        try {
            currentUser = getCurrentUser();
        } catch (Exception e) {
            currentUser = null; // hệ thống tự đổi (hiếm), không có user cụ thể
        }
        EventStatusHistory history = EventStatusHistory.builder()
                .event(event)
                .fromStatus(from)
                .toStatus(to)
                .changedBy(currentUser)
                .note(note)
                .build();
        eventStatusHistoryRepository.save(history);
    }

    private EventDetail saveEventDetail(Long eventId, EventRequest request) {
        EventDetail detail = eventDetailRepository.findByEventId(eventId)
                .orElse(EventDetail.builder().eventId(eventId).build());
        detail.setType(request.getType().name());
        detail.setAttributes(request.getAttributes());
        return eventDetailRepository.save(detail);
    }

    private EventDetail getEventDetail(Long eventId) {
        return eventDetailRepository.findByEventId(eventId).orElse(null);
    }

    private EventResponse toResponse(Event event, EventDetail detail) {
        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .type(event.getType())
                .description(event.getDescription())
                .posterUrl(event.getPosterUrl())
                .status(event.getStatus())
                .producerSharePercent(event.getProducerSharePercent())
                .avgRating(event.getAvgRating())
                .attributes(detail != null ? detail.getAttributes() : null)
                .createdAt(event.getCreatedAt())
                .build();
    }
}