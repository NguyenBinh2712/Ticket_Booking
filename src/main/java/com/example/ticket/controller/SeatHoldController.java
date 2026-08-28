package com.example.ticket.controller;

import com.example.ticket.dto.ApiResponse;
import com.example.ticket.dto.booking.HoldSeatRequest;
import com.example.ticket.dto.booking.HoldSeatResponse;
import com.example.ticket.service.SeatHoldService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/seats")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("isAuthenticated()")
public class SeatHoldController {

    SeatHoldService seatHoldService;

    @PostMapping("/hold")
    public ApiResponse<HoldSeatResponse> hold(@RequestBody @Valid HoldSeatRequest request) {
        ApiResponse<HoldSeatResponse> response = new ApiResponse<>();
        response.setResult(seatHoldService.holdSeats(request));
        response.setMessage("Đã giữ ghế trong 10 phút");
        return response;
    }

    @PostMapping("/release")
    public ApiResponse<Void> release(@RequestParam Long showtimeId, @RequestParam List<Long> seatIds) {
        seatHoldService.releaseSeats(showtimeId, seatIds);
        ApiResponse<Void> response = new ApiResponse<>();
        response.setMessage("Đã nhả ghế");
        return response;
    }
}