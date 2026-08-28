// controller/RevenueTransactionController.java
package com.example.ticket.controller;

import com.example.ticket.dto.ApiResponse;
import com.example.ticket.dto.revenue.RevenueTransactionResponse;
import com.example.ticket.entity.*;
import com.example.ticket.exception.AppException;
import com.example.ticket.exception.ErrorCode;
import com.example.ticket.repository.ProducerProfileRepository;
import com.example.ticket.repository.RevenueTransactionRepository;
import com.example.ticket.repository.UserRepository;
import com.example.ticket.repository.VenueProfileRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RevenueTransactionController {

    RevenueTransactionRepository revenueTransactionRepository;
    ProducerProfileRepository producerProfileRepository;
    VenueProfileRepository venueProfileRepository;
    UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    @PreAuthorize("hasRole('PRODUCER')")
    @GetMapping("/producers/me/revenue")
    public ApiResponse<List<RevenueTransactionResponse>> myProducerRevenue() {
        ProducerProfile producer = producerProfileRepository.findByUser(getCurrentUser())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCER_PROFILE_NOT_FOUND));
        List<RevenueTransactionResponse> result = revenueTransactionRepository
                .findByContract_Event_Producer(producer).stream().map(this::toResponse).collect(Collectors.toList());
        ApiResponse<List<RevenueTransactionResponse>> response = new ApiResponse<>();
        response.setResult(result);
        return response;
    }

    @PreAuthorize("hasRole('VENUE_OWNER')")
    @GetMapping("/venues/me/revenue")
    public ApiResponse<List<RevenueTransactionResponse>> myVenueRevenue() {
        VenueProfile venue = venueProfileRepository.findByUser(getCurrentUser())
                .orElseThrow(() -> new AppException(ErrorCode.VENUE_PROFILE_NOT_FOUND));
        List<RevenueTransactionResponse> result = revenueTransactionRepository
                .findByContract_Venue(venue).stream().map(this::toResponse).collect(Collectors.toList());
        ApiResponse<List<RevenueTransactionResponse>> response = new ApiResponse<>();
        response.setResult(result);
        return response;
    }

    private RevenueTransactionResponse toResponse(RevenueTransaction t) {
        return RevenueTransactionResponse.builder()
                .id(t.getId())
                .bookingCode(t.getBooking().getBookingCode())
                .eventTitle(t.getContract().getEvent().getTitle())
                .totalAmount(t.getTotalAmount())
                .producerAmount(t.getProducerAmount())
                .venueAmount(t.getVenueAmount())
                .adminAmount(t.getAdminAmount())
                .status(t.getStatus())
                .createdAt(t.getCreatedAt())
                .build();
    }
}