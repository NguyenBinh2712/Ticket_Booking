package com.example.ticket.repository;

import com.example.ticket.entity.Booking;
import com.example.ticket.entity.User;
import com.example.ticket.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByCustomer(User customer);
    Optional<Booking> findByIdAndCustomer(Long id, User customer);
    boolean existsByBookingCode(String code);
    List<Booking> findByStatusAndPaymentDeadlineBefore(BookingStatus status, LocalDateTime time);
}