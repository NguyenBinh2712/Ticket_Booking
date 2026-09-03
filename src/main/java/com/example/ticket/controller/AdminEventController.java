package com.example.ticket.controller;

import com.example.ticket.dto.ApiResponse;
import com.example.ticket.dto.contract.*;
import com.example.ticket.dto.event.EventResponse;
import com.example.ticket.entity.Event;
import com.example.ticket.service.AdminEventService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('ADMIN')")
public class AdminEventController {

    AdminEventService adminEventService;

    @GetMapping("/pending")
    public ApiResponse<List<Event>> getPendingEvents() {
        ApiResponse<List<Event>> response = new ApiResponse<>();
        response.setResult(adminEventService.getPendingEvents());
        return response;
    }

    @PutMapping("/{eventId}/approve")
    public ApiResponse<Void> approve(@PathVariable Long eventId) {
        adminEventService.approveEventForMatching(eventId);
        ApiResponse<Void> response = new ApiResponse<>();
        response.setMessage("Đã duyệt, bắt đầu tìm Venue");
        return response;
    }

    @PutMapping("/{eventId}/reject")
    public ApiResponse<Void> reject(@PathVariable Long eventId, @RequestParam String reason) {
        adminEventService.rejectEvent(eventId, reason);
        ApiResponse<Void> response = new ApiResponse<>();
        response.setMessage("Đã từ chối sự kiện");
        return response;
    }

    @PostMapping("/{eventId}/contracts")
    public ApiResponse<ContractResponse> createOffer(
            @PathVariable Long eventId, @RequestBody @Valid ContractCreateRequest request) {
        ApiResponse<ContractResponse> response = new ApiResponse<>();
        response.setResult(adminEventService.createContractOffer(eventId, request));
        response.setMessage("Đã gửi lời mời tới Venue");
        return response;
    }

    @GetMapping("/{eventId}/contracts")
    public ApiResponse<List<ContractResponse>> getContracts(@PathVariable Long eventId) {
        ApiResponse<List<ContractResponse>> response = new ApiResponse<>();
        response.setResult(adminEventService.getContractsOfEvent(eventId));
        return response;
    }

    @PutMapping("/{eventId}/publish")
    public ApiResponse<Void> publish(@PathVariable Long eventId) {
        adminEventService.confirmAndPublish(eventId);
        ApiResponse<Void> response = new ApiResponse<>();
        response.setMessage("Sự kiện đã được công khai");
        return response;
    }
}