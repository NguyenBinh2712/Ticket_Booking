package com.example.ticket.controller;

import com.example.ticket.dto.ApiResponse;
import com.example.ticket.dto.event.PublicEventResponse;
import com.example.ticket.dto.event.PublicShowtimeResponse;
import com.example.ticket.dto.event.ShowtimeSeatMapResponse;
import com.example.ticket.enums.EventType;
import com.example.ticket.service.PublicEventService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublicEventController {

    PublicEventService publicEventService;

    @GetMapping("/events")
    public ApiResponse<List<PublicEventResponse>> getEvents(@RequestParam(required = false) EventType type) {
        ApiResponse<List<PublicEventResponse>> response = new ApiResponse<>();
        response.setResult(publicEventService.getPublishedEvents(type));
        return response;
    }

    @GetMapping("/events/{eventId}")
    public ApiResponse<PublicEventResponse> getEventDetail(@PathVariable Long eventId) {
        ApiResponse<PublicEventResponse> response = new ApiResponse<>();
        response.setResult(publicEventService.getEventDetail(eventId));
        return response;
    }

    @GetMapping("/events/{eventId}/showtimes")
    public ApiResponse<List<PublicShowtimeResponse>> getShowtimesOfEvent(@PathVariable Long eventId) {
        ApiResponse<List<PublicShowtimeResponse>> response = new ApiResponse<>();
        response.setResult(publicEventService.getShowtimesOfEvent(eventId));
        return response;
    }

    @GetMapping("/showtimes")
    public ApiResponse<List<PublicShowtimeResponse>> searchShowtimes(
            @RequestParam(required = false) EventType type,
            @RequestParam(required = false) String city) {
        ApiResponse<List<PublicShowtimeResponse>> response = new ApiResponse<>();
        response.setResult(publicEventService.searchShowtimes(type, city));
        return response;
    }

    @GetMapping("/showtimes/{showtimeId}")
    public ApiResponse<PublicShowtimeResponse> getShowtimeDetail(@PathVariable Long showtimeId) {
        ApiResponse<PublicShowtimeResponse> response = new ApiResponse<>();
        response.setResult(publicEventService.getShowtimeDetail(showtimeId));
        return response;
    }

    @GetMapping("/showtimes/{showtimeId}/seats")
    public ApiResponse<ShowtimeSeatMapResponse> getSeatMap(@PathVariable Long showtimeId) {
        ApiResponse<ShowtimeSeatMapResponse> response = new ApiResponse<>();
        response.setResult(publicEventService.getSeatMap(showtimeId));
        return response;
    }
}