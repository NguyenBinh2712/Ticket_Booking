package com.example.ticket.repository;

import com.example.ticket.entity.Booking;
import com.example.ticket.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByBooking(Booking booking);
    Optional<Payment> findByTxnRef(String txnRef);
}