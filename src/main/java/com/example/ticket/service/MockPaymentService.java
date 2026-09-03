package com.example.ticket.service;

import com.example.ticket.entity.Booking;
import com.example.ticket.entity.Payment;
import com.example.ticket.entity.User;
import com.example.ticket.enums.BookingStatus;
import com.example.ticket.enums.PaymentMethod;
import com.example.ticket.enums.PaymentStatus;
import com.example.ticket.exception.AppException;
import com.example.ticket.exception.ErrorCode;
import com.example.ticket.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class MockPaymentService {

    PaymentRepository paymentRepository;
    PaymentConfirmationService paymentConfirmationService;

    public String createMockPayment(Booking booking) {
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new AppException(ErrorCode.BOOKING_EXPIRED);
        }

        String txnRef = "MOCK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Payment payment = paymentRepository.findByBooking(booking)
                .orElse(Payment.builder().booking(booking).build());
        payment.setMethod(PaymentMethod.MOCK_GATEWAY);
        payment.setAmount(booking.getTotalPrice());
        payment.setTxnRef(txnRef);
        payment.setStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);

        return txnRef;
    }

    public void simulateSuccess(String txnRef, User currentUser) {
        Payment payment = paymentRepository.findByTxnRef(txnRef)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        if (!payment.getBooking().getCustomer().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.BOOKING_NOT_OWNED_BY_USER);
        }
        if (payment.getMethod() != PaymentMethod.MOCK_GATEWAY) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_METHOD);
        }
        paymentConfirmationService.confirmSuccess(payment, "MOCK-" + System.currentTimeMillis(), "00");
    }

    public void simulateFailure(String txnRef, User currentUser) {
        Payment payment = paymentRepository.findByTxnRef(txnRef)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        if (!payment.getBooking().getCustomer().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.BOOKING_NOT_OWNED_BY_USER);
        }
        if (payment.getMethod() != PaymentMethod.MOCK_GATEWAY) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_METHOD);
        }
        paymentConfirmationService.markFailed(payment, "99");
    }
}