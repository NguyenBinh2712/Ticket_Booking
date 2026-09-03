package com.example.ticket.service;

import com.example.ticket.entity.Booking;
import com.example.ticket.enums.BookingStatus;
import com.example.ticket.repository.BookingRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingExpiryScheduler {

    BookingRepository bookingRepository;
    PaymentConfirmationService paymentConfirmationService;

    @Scheduled(fixedRate = 60000)
    public void releaseExpiredBookings() {
        List<Booking> expired = bookingRepository
                .findByStatusAndPaymentDeadlineBefore(BookingStatus.PENDING, LocalDateTime.now());
        for (Booking booking : expired) {
            paymentConfirmationService.expireBooking(booking);
        }
    }
}