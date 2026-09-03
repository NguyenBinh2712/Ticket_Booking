package com.example.ticket.repository;

import com.example.ticket.entity.Booking;
import com.example.ticket.entity.BookingSeat;
import com.example.ticket.entity.VenueProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {
    List<BookingSeat> findByShowtimeId(Long showtimeId);
    boolean existsByShowtimeIdAndSeat_Id(Long showtimeId, Long seatId);
    List<BookingSeat> findByBooking(Booking booking);

    @Query(
            "SELECT COUNT(bs) FROM BookingSeat bs " +
                    "WHERE bs.booking.status = 'CONFIRMED' AND bs.booking.showtime.contract.venue = :venue")
    long countConfirmedSeatsByVenue(@Param("venue") VenueProfile venue);
}