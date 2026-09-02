package com.example.ticket.repository;

import com.example.ticket.entity.Room;
import com.example.ticket.entity.Seat;
import com.example.ticket.entity.VenueProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat,Long> {
    List<Seat> findByRoomOrderBySeatRowAscSeatNumberAsc(Room room);
    @Query(
            "SELECT COUNT(s) FROM Seat s WHERE s.active = true AND s.room.venue = :venue")
    long countActiveSeatsByVenue(@Param("venue") VenueProfile venue);
}
