// service/PublicEventService.java
package com.example.ticket.service;

import com.example.ticket.document.EventDetail;
import com.example.ticket.dto.event.PublicEventResponse;
import com.example.ticket.dto.event.SeatAvailabilityResponse;
import com.example.ticket.dto.room_seat.SeatResponse;
import com.example.ticket.dto.event.PublicShowtimeResponse;
import com.example.ticket.dto.event.ShowtimeSeatMapResponse;
import com.example.ticket.entity.Event;
import com.example.ticket.entity.Room;
import com.example.ticket.entity.Showtime;
import com.example.ticket.enums.EventStatus;
import com.example.ticket.enums.EventType;
import com.example.ticket.exception.AppException;
import com.example.ticket.exception.ErrorCode;
import com.example.ticket.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublicEventService {
    BookingSeatRepository bookingSeatRepository;
    SeatHoldService seatHoldService;

    // Sự kiện ở các trạng thái này mới được xem công khai
    private static final Set<EventStatus> VISIBLE_STATUSES =
            EnumSet.of(EventStatus.PUBLISHED, EventStatus.ONGOING, EventStatus.COMPLETED);
    EventRepository eventRepository;
    EventDetailRepository eventDetailRepository;
    ShowtimeRepository showtimeRepository;
    SeatRepository seatRepository;

    public List<PublicEventResponse> getPublishedEvents(EventType type) {
        List<Event> events = (type != null)
                ? eventRepository.findByStatusAndType(EventStatus.PUBLISHED, type)
                : eventRepository.findByStatus(EventStatus.PUBLISHED);
        return events.stream().map(this::toEventResponse).collect(Collectors.toList());
    }

    public PublicEventResponse getEventDetail(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        if (!VISIBLE_STATUSES.contains(event.getStatus())) {
            throw new AppException(ErrorCode.EVENT_NOT_FOUND);
        }
        return toEventResponse(event);
    }

    public List<PublicShowtimeResponse> getShowtimesOfEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        if (!VISIBLE_STATUSES.contains(event.getStatus())) {
            throw new AppException(ErrorCode.EVENT_NOT_FOUND);
        }
        return showtimeRepository.findByContract_EventOrderByStartTimeAsc(event)
                .stream().map(this::toShowtimeResponse).collect(Collectors.toList());
    }

    public List<PublicShowtimeResponse> searchShowtimes(EventType type, String city) {
        return showtimeRepository.searchPublicShowTimes(type, city)
                .stream().map(this::toShowtimeResponse).collect(Collectors.toList());
    }

    public PublicShowtimeResponse getShowtimeDetail(Long showtimeId) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new AppException(ErrorCode.SHOWTIME_NOT_FOUND));
        if (!VISIBLE_STATUSES.contains(showtime.getContract().getEvent().getStatus())) {
            throw new AppException(ErrorCode.SHOWTIME_NOT_FOUND);
        }
        return toShowtimeResponse(showtime);
    }

    public ShowtimeSeatMapResponse getSeatMap(Long showtimeId) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new AppException(ErrorCode.SHOWTIME_NOT_FOUND));
        if (!VISIBLE_STATUSES.contains(showtime.getContract().getEvent().getStatus())) {
            throw new AppException(ErrorCode.SHOWTIME_NOT_FOUND);
        }

        Room room = showtime.getContract().getRoom();
        List<SeatResponse> seats = seatRepository.findByRoomOrderBySeatRowAscSeatNumberAsc(room)
                .stream().map(this::toSeatResponse).collect(Collectors.toList());

        return ShowtimeSeatMapResponse.builder()
                .showtimeId(showtime.getId())
                .roomId(room.getId())
                .roomName(room.getName())
                .totalRows(room.getTotalRows())
                .totalColumns(room.getTotalColumns())
                .seats(seats)
                .build();
    }

    private PublicEventResponse toEventResponse(Event event) {
        EventDetail detail = eventDetailRepository.findByEventId(event.getId()).orElse(null);
        return PublicEventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .type(event.getType())
                .description(event.getDescription())
                .posterUrl(event.getPosterUrl())
                .status(event.getStatus())
                .avgRating(event.getAvgRating())
                .attributes(detail != null ? detail.getAttributes() : null)
                .build();
    }

    private PublicShowtimeResponse toShowtimeResponse(Showtime s) {
        Event event = s.getContract().getEvent();
        var venue = s.getContract().getVenue();
        var room = s.getContract().getRoom();
        return PublicShowtimeResponse.builder()
                .id(s.getId())
                .eventId(event.getId())
                .eventTitle(event.getTitle())
                .eventPosterUrl(event.getPosterUrl())
                .venueId(venue.getId())
                .venueName(venue.getVenueName())
                .venueCity(venue.getCity())
                .venueAddress(venue.getAddress())
                .roomId(room != null ? room.getId() : null)
                .roomName(room != null ? room.getName() : null)
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .ticketPrice(s.getTicketPrice())
                .status(s.getStatus())
                .build();
    }
    public List<SeatAvailabilityResponse> getSeatAvailability(Long showtimeId) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new AppException(ErrorCode.SHOWTIME_NOT_FOUND));

        Room room = showtime.getContract().getRoom();
        List<Long> bookedSeatIds = bookingSeatRepository.findByShowtimeId(showtimeId)
                .stream().map(bs -> bs.getSeat().getId()).toList();

        return seatRepository.findByRoomOrderBySeatRowAscSeatNumberAsc(room).stream()
                .map(seat -> {
                    String status;
                    if (bookedSeatIds.contains(seat.getId())) {
                        status = "BOOKED";
                    } else if (seatHoldService.getHoldOwner(showtimeId, seat.getId()) != null) {
                        status = "HELD";
                    } else {
                        status = "AVAILABLE";
                    }
                    return SeatAvailabilityResponse.builder()
                            .seatId(seat.getId())
                            .seatRow(seat.getSeatRow())
                            .seatNumber(seat.getSeatNumber())
                            .seatTypeName(seat.getSeatType().getName())
                            .extraPrice(seat.getSeatType().getExtraPrice().toString())
                            .seatSpan(seat.getSeatSpan())
                            .active(seat.getActive())
                            .status(seat.getActive() ? status : "INACTIVE")
                            .build();
                })
                .collect(Collectors.toList());
    }

    private SeatResponse toSeatResponse(com.example.ticket.entity.Seat seat) {
        return SeatResponse.builder()
                .id(seat.getId())
                .seatRow(seat.getSeatRow())
                .seatNumber(seat.getSeatNumber())
                .seatTypeId(seat.getSeatType().getId())
                .seatTypeName(seat.getSeatType().getName())
                .extraPrice(seat.getSeatType().getExtraPrice().toString())
                .active(seat.getActive())
                .seatSpan(seat.getSeatSpan())
                .build();
    }
}