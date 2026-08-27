package com.example.ticket.service;

import com.example.ticket.dto.contract.*;
import com.example.ticket.entity.*;
import com.example.ticket.enums.ContractStatus;
import com.example.ticket.enums.EventStatus;
import com.example.ticket.enums.ShowtimeStatus;
import com.example.ticket.exception.AppException;
import com.example.ticket.exception.ErrorCode;
import com.example.ticket.repository.*;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class VenueContractService {

    EventVenueContractRepository contractRepository;
    VenueProfileRepository venueProfileRepository;
    RoomRepository roomRepository;
    ShowtimeRepository showtimeRepository;
    UserRepository userRepository;
    EventService eventService;

    private VenueProfile getCurrentVenue() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return venueProfileRepository.findByUser(user)
                .orElseThrow(() -> new AppException(ErrorCode.VENUE_PROFILE_NOT_FOUND));
    }

    public List<ContractResponse> getMyPendingContracts() {
        VenueProfile venue = getCurrentVenue();
        return contractRepository.findByVenueAndStatus(venue, ContractStatus.PROPOSED)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public void acceptContract(Long contractId, ContractAcceptRequest request) {
        VenueProfile venue = getCurrentVenue();
        EventVenueContract contract = contractRepository.findByIdAndVenue(contractId, venue)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));

        if (contract.getStatus() != ContractStatus.PROPOSED) {
            throw new AppException(ErrorCode.CONTRACT_ALREADY_RESPONDED);
        }

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
        if (!room.getVenue().getId().equals(venue.getId())) {
            throw new AppException(ErrorCode.ROOM_NOT_OWNED_BY_VENUE);
        }
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new AppException(ErrorCode.SHOWTIME_TIME_OVERLAPPED);
        }

        boolean overlapped = showtimeRepository.existsByRoomAndStatusNotAndStartTimeLessThanAndEndTimeGreaterThan(
                room, ShowtimeStatus.CANCELLED, request.getEndTime(), request.getStartTime());
        if (overlapped) {
            throw new AppException(ErrorCode.SHOWTIME_TIME_OVERLAPPED);
        }

        contract.setRoom(room);
        contract.setStatus(ContractStatus.ACCEPTED);
        contractRepository.save(contract);

        Showtime showtime = Showtime.builder()
                .contract(contract)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .ticketPrice(contract.getTicketBasePrice())
                .status(ShowtimeStatus.SCHEDULED)
                .build();
        showtimeRepository.save(showtime);

        eventService.changeStatus(contract.getEvent(), EventStatus.VENUE_ACCEPTED,
                "Venue " + venue.getVenueName() + " đã chấp nhận");
    }

    public void rejectContract(Long contractId, ContractRejectRequest request) {
        VenueProfile venue = getCurrentVenue();
        EventVenueContract contract = contractRepository.findByIdAndVenue(contractId, venue)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));

        if (contract.getStatus() != ContractStatus.PROPOSED) {
            throw new AppException(ErrorCode.CONTRACT_ALREADY_RESPONDED);
        }

        contract.setStatus(ContractStatus.REJECTED);
        contract.setRejectReason(request.getRejectReason());
        contractRepository.save(contract);

        Event event = contract.getEvent();
        if (event.getStatus() == EventStatus.PENDING_VENUE_APPROVAL) {
            eventService.changeStatus(event, EventStatus.MATCHING,
                    "Venue " + venue.getVenueName() + " từ chối: " + request.getRejectReason());
        }
    }

    private ContractResponse toResponse(EventVenueContract c) {
        return ContractResponse.builder()
                .id(c.getId())
                .eventId(c.getEvent().getId())
                .eventTitle(c.getEvent().getTitle())
                .venueId(c.getVenue().getId())
                .venueName(c.getVenue().getVenueName())
                .roomId(c.getRoom() != null ? c.getRoom().getId() : null)
                .producerSharePercent(c.getProducerSharePercent())
                .venueSharePercent(c.getVenueSharePercent())
                .adminCommissionPercent(c.getAdminCommissionPercent())
                .ticketBasePrice(c.getTicketBasePrice())
                .status(c.getStatus())
                .createdAt(c.getCreatedAt())
                .build();
    }
}