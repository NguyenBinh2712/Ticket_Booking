package com.example.ticket.repository;

import com.example.ticket.entity.Booking;
import com.example.ticket.entity.ProducerProfile;
import com.example.ticket.entity.RevenueTransaction;
import com.example.ticket.entity.VenueProfile;
import com.example.ticket.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RevenueTransactionRepository extends JpaRepository<RevenueTransaction, Long> {
    Optional<RevenueTransaction> findByBooking(Booking booking);
    List<RevenueTransaction> findByStatus(TransactionStatus status);
    List<RevenueTransaction> findByContract_Event_Producer(ProducerProfile producer);
    List<RevenueTransaction> findByContract_Venue(VenueProfile venue);
}