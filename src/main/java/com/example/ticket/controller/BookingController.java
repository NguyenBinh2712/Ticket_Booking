package com.example.ticket.controller;

import com.example.ticket.dto.ApiResponse;
import com.example.ticket.dto.booking.BookingConfirmRequest;
import com.example.ticket.dto.booking.BookingResponse;
import com.example.ticket.service.BookingService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("isAuthenticated()")
public class BookingController {

    BookingService bookingService;

    @PostMapping("/confirm")
    public ApiResponse<BookingResponse> confirm(@RequestBody @Valid BookingConfirmRequest request) {
        ApiResponse<BookingResponse> response = new ApiResponse<>();
        response.setResult(bookingService.confirmBooking(request));
        response.setMessage("Đặt vé thành công, chờ thanh toán");
        return response;
    }

    @GetMapping("/me")
    public ApiResponse<List<BookingResponse>> myBookings() {
        ApiResponse<List<BookingResponse>> response = new ApiResponse<>();
        response.setResult(bookingService.getMyBookings());
        return response;
    }

    @GetMapping("/me/{id}")
    public ApiResponse<BookingResponse> myBookingDetail(@PathVariable Long id) {
        ApiResponse<BookingResponse> response = new ApiResponse<>();
        response.setResult(bookingService.getMyBookingDetail(id));
        return response;
    }
}