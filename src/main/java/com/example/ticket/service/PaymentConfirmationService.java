package com.example.ticket.service;

import com.example.ticket.entity.Booking;
import com.example.ticket.entity.Payment;
import com.example.ticket.enums.BookingStatus;
import com.example.ticket.enums.PaymentStatus;
import com.example.ticket.repository.BookingRepository;
import com.example.ticket.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class PaymentConfirmationService {

    PaymentRepository paymentRepository;
    BookingRepository bookingRepository;
    RevenueTransactionService revenueTransactionService;

    // Trả về false nếu đã SUCCESS từ trước -- chặn xử lý trùng khi callback gọi lại nhiều lần
    public boolean confirmSuccess(Payment payment, String gatewayTransactionNo, String responseCode) {
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return false;
        }

        payment.setGatewayTransactionNo(gatewayTransactionNo);
        payment.setResponseCode(responseCode);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        Booking booking = payment.getBooking();
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        revenueTransactionService.createForBooking(booking);
        return true;
    }

    public void markFailed(Payment payment, String responseCode) {
        payment.setResponseCode(responseCode);
        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);
    }
}