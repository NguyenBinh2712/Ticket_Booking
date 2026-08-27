package com.example.ticket.repository;

import com.example.ticket.entity.Room;
import com.example.ticket.entity.Showtime;
import com.example.ticket.enums.ShowtimeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {
    boolean existsByRoomAndStatusNotAndStartTimeLessThanAndEndTimeGreaterThan(
            Room room, ShowtimeStatus excludedStatus, LocalDateTime endTime, LocalDateTime startTime);
}