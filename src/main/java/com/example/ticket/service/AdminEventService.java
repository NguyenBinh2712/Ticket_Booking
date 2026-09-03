package com.example.ticket.service;

import com.example.ticket.dto.contract.*;
import com.example.ticket.dto.event.EventResponse;
import com.example.ticket.entity.*;
import com.example.ticket.enums.ContractStatus;
import com.example.ticket.enums.EventStatus;
import com.example.ticket.enums.ProfileStatus;
import com.example.ticket.exception.AppException;
import com.example.ticket.exception.ErrorCode;
import com.example.ticket.repository.*;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class AdminEventService {

    EventRepository eventRepository;
    EventVenueContractRepository contractRepository;
    VenueProfileRepository venueProfileRepository;
    EventService eventService;

    public List<Event> getPendingEvents() {
        return eventRepository.findByStatus(EventStatus.SUBMITTED);
    }

    // Admin duyệt nội dung Event -> cho phép bắt đầu tìm Venue
    public void approveEventForMatching(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        eventService.changeStatus(event, EventStatus.ADMIN_REVIEWING, "Admin duyệt nội dung");
        eventService.changeStatus(event, EventStatus.MATCHING, "Bắt đầu tìm Venue phù hợp");
    }

    public void rejectEvent(Long eventId, String reason) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        eventService.changeStatus(event, EventStatus.CANCELLED, "Admin từ chối: " + reason);
    }

    // Admin gửi lời mời tới 1 Venue
    public ContractResponse createContractOffer(Long eventId, ContractCreateRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        if (event.getStatus() != EventStatus.MATCHING) {
            throw new AppException(ErrorCode.INVALID_EVENT_STATUS_TRANSITION);
        }
        VenueProfile venue = venueProfileRepository.findById(request.getVenueId())
                .orElseThrow(() -> new AppException(ErrorCode.VENUE_PROFILE_NOT_FOUND));
        if (venue.getStatus() != ProfileStatus.VERIFIED) {
            throw new AppException(ErrorCode.VENUE_NOT_VERIFIED);
        }
        BigDecimal adminPercent = new BigDecimal("2.5");
        BigDecimal producerPercent = event.getProducerSharePercent();
        BigDecimal venuePercent = BigDecimal.valueOf(100)
                .subtract(producerPercent)
                .subtract(adminPercent);
        if(venuePercent.signum() < 0) {
            throw new AppException(ErrorCode.INVALID_REVENUE_SPLIT);}

        EventVenueContract contract = EventVenueContract.builder()
                .event(event)
                .venue(venue)
                .producerSharePercent(producerPercent)
                .venueSharePercent(venuePercent)
                .adminCommissionPercent(new BigDecimal("2.5"))
                .ticketBasePrice(request.getTicketBasePrice())
                .status(ContractStatus.PROPOSED)
                .build();
        contract = contractRepository.save(contract);

        if (event.getStatus() == EventStatus.MATCHING) {
            eventService.changeStatus(event, EventStatus.PENDING_VENUE_APPROVAL, "Gửi lời mời tới " + venue.getVenueName());
        }
        return toResponse(contract);
    }

    public List<ContractResponse> getContractsOfEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        return contractRepository.findByEvent(event).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public void confirmAndPublish(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        boolean hasAccepted = contractRepository.findByEvent(event).stream()
                .anyMatch(c -> c.getStatus() == ContractStatus.ACCEPTED);
        if (!hasAccepted) {
            throw new AppException(ErrorCode.EVENT_NOT_READY_TO_PUBLISH);
        }

        eventService.changeStatus(event, EventStatus.CONTRACT_CONFIRMED, "Admin xác nhận hợp đồng");
        eventService.changeStatus(event, EventStatus.PUBLISHED, "Công khai sự kiện");
    }

    private ContractResponse toResponse(EventVenueContract c) {
        return ContractResponse.builder()
                .id(c.getId())
                .eventId(c.getEvent().getId())
                .eventTitle(c.getEvent().getTitle())
                .venueId(c.getVenue().getId())
                .venueName(c.getVenue().getVenueName())
                .roomId(c.getRoom() != null ? c.getRoom().getId() : null)
                .roomName(c.getRoom() != null ? c.getRoom().getName() : null)
                .producerSharePercent(c.getProducerSharePercent())
                .venueSharePercent(c.getVenueSharePercent())
                .adminCommissionPercent(c.getAdminCommissionPercent())
                .ticketBasePrice(c.getTicketBasePrice())
                .status(c.getStatus())
                .rejectReason(c.getRejectReason())
                .createdAt(c.getCreatedAt())
                .build();
    }
}