package com.example.ticket.controller;

import com.example.ticket.dto.ApiResponse;
import com.example.ticket.dto.event.*;
import com.example.ticket.service.EventService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/producers/events")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('PRODUCER')")
public class EventController {

    EventService eventService;

    @PostMapping
    public ApiResponse<EventResponse> createEvent(@RequestBody @Valid EventRequest request) {
        ApiResponse<EventResponse> response = new ApiResponse<>();
        response.setResult(eventService.createEvent(request));
        response.setMessage("Tạo sự kiện thành công (nháp)");
        return response;
    }

    @PutMapping("/{eventId}")
    public ApiResponse<EventResponse> updateEvent(@PathVariable Long eventId, @RequestBody @Valid EventRequest request) {
        ApiResponse<EventResponse> response = new ApiResponse<>();
        response.setResult(eventService.updateEvent(eventId, request));
        response.setMessage("Cập nhật sự kiện thành công");
        return response;
    }

    @PutMapping("/{eventId}/submit")
    public ApiResponse<EventResponse> submitEvent(@PathVariable Long eventId) {
        ApiResponse<EventResponse> response = new ApiResponse<>();
        response.setResult(eventService.submitEvent(eventId));
        response.setMessage("Đã gửi sự kiện cho Admin duyệt");
        return response;
    }

    @GetMapping
    public ApiResponse<List<EventResponse>> getMyEvents() {
        ApiResponse<List<EventResponse>> response = new ApiResponse<>();
        response.setResult(eventService.getMyEvents());
        return response;
    }

    @GetMapping("/{eventId}")
    public ApiResponse<EventResponse> getMyEventDetail(@PathVariable Long eventId) {
        ApiResponse<EventResponse> response = new ApiResponse<>();
        response.setResult(eventService.getMyEventDetail(eventId));
        return response;
    }

    @GetMapping("/{eventId}/history")
    public ApiResponse<List<EventStatusHistoryResponse>> getStatusHistory(@PathVariable Long eventId) {
        ApiResponse<List<EventStatusHistoryResponse>> response = new ApiResponse<>();
        response.setResult(eventService.getStatusHistory(eventId));
        return response;
    }
}