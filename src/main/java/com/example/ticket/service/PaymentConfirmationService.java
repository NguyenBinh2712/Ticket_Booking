package com.example.ticket.service;

import com.example.ticket.entity.Booking;
import com.example.ticket.entity.BookingSeat;
import com.example.ticket.entity.Payment;
import com.example.ticket.enums.BookingStatus;
import com.example.ticket.enums.PaymentStatus;
import com.example.ticket.exception.AppException;
import com.example.ticket.exception.ErrorCode;
import com.example.ticket.repository.BookingRepository;
import com.example.ticket.repository.BookingSeatRepository;
import com.example.ticket.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class PaymentConfirmationService {

    PaymentRepository paymentRepository;
    BookingRepository bookingRepository;
    BookingSeatRepository bookingSeatRepository;
    RevenueTransactionService revenueTransactionService;

    public boolean confirmSuccess(Payment payment,  String gatewayTransactionNo, String responseCode) {
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return false;
        }
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new AppException(ErrorCode.PAYMENT_ALREADY_PROCESSED);
        }
        Booking booking = bookingRepository
                .findByIdForUpdate(payment.getBooking().getId())
                .orElseThrow(() ->
                        new AppException(ErrorCode.BOOKING_NOT_FOUND));
        if (booking.getStatus() != BookingStatus.PENDING) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setResponseCode("BOOKING_NOT_PENDING");
            return false;
        }

        if (booking.getPaymentDeadline() == null || !booking.getPaymentDeadline().isAfter(LocalDateTime.now())) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setResponseCode("BOOKING_PAYMENT_DEADLINE_EXPIRED");
            expireBooking(booking);
            return false;
        }

        payment.setGatewayTransactionNo(gatewayTransactionNo);
        payment.setResponseCode(responseCode);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        booking.setStatus(BookingStatus.CONFIRMED);
        paymentRepository.save(payment);
        bookingRepository.save(booking);

        revenueTransactionService.createForBooking(booking);

        return true;
    }
    public void markFailed(Payment payment, String responseCode) {
        if (payment.getStatus() != PaymentStatus.PENDING) {
            return;
        }
        payment.setResponseCode(responseCode);
        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);
        cancelBooking(payment.getBooking());
    }


    public void cancelBooking(Booking booking) {
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            throw new AppException(ErrorCode.BOOKING_CANCEL_NOT_ALLOWED);
        }

        deleteBookingSeats(booking);

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    public void expireBooking(Booking booking) {
        if (booking.getStatus() != BookingStatus.PENDING) {
            return;
        }

        deleteBookingSeats(booking);

        booking.setStatus(BookingStatus.EXPIRED);
        bookingRepository.save(booking);
    }

    private void deleteBookingSeats(Booking booking) {
        List<BookingSeat> seats =
                bookingSeatRepository.findByBooking(booking);

        bookingSeatRepository.deleteAll(seats);
    }

}