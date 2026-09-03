// controller/PaymentController.java
package com.example.ticket.controller;

import com.example.ticket.dto.ApiResponse;
import com.example.ticket.entity.Booking;
import com.example.ticket.entity.User;
import com.example.ticket.enums.PaymentMethod;
import com.example.ticket.exception.AppException;
import com.example.ticket.exception.ErrorCode;
import com.example.ticket.repository.BookingRepository;
import com.example.ticket.repository.UserRepository;
import com.example.ticket.service.MockPaymentService;
import com.example.ticket.service.MomoPaymentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {

    MomoPaymentService momoPaymentService;
    MockPaymentService mockPaymentService;
    BookingRepository bookingRepository;
    UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public ApiResponse<String> create(@RequestParam Long bookingId, @RequestParam PaymentMethod method) {
        User user = getCurrentUser();
        Booking booking = bookingRepository.findByIdAndCustomer(bookingId, user)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        String result = switch (method) {
            case MOMO -> momoPaymentService.createPaymentUrl(booking);
            case MOCK_GATEWAY -> mockPaymentService.createMockPayment(booking);
        };

        ApiResponse<String> response = new ApiResponse<>();
        response.setResult(result);
        return response;
    }

    @PostMapping(value = "/momo/ipn", produces = MediaType.APPLICATION_JSON_VALUE)
    public String momoIpn(@RequestBody Map<String, Object> body) {
        return momoPaymentService.handleIpn(body);
    }

    @GetMapping("/momo/return")
    public ApiResponse<Boolean> momoReturn(@RequestParam String resultCode) {
        ApiResponse<Boolean> response = new ApiResponse<>();
        response.setResult("0".equals(resultCode));
        return response;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/mock/success")
    public ApiResponse<Void> mockSuccess(@RequestParam String txnRef) {
        User user = getCurrentUser();
        mockPaymentService.simulateSuccess(txnRef,user);
        ApiResponse<Void> response = new ApiResponse<>();
        response.setMessage("Thanh toán (giả lập) thành công");
        return response;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/mock/fail")
    public ApiResponse<Void> mockFail(@RequestParam String txnRef) {
        User user = getCurrentUser();
        mockPaymentService.simulateFailure(txnRef, user);
        ApiResponse<Void> response = new ApiResponse<>();
        response.setMessage("Thanh toán (giả lập) thất bại");
        return response;
    }
}