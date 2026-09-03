// repository/ShowtimeRepository.java
package com.example.ticket.repository;

import com.example.ticket.entity.Event;
import com.example.ticket.entity.Room;
import com.example.ticket.entity.Showtime;
import com.example.ticket.entity.VenueProfile;
import com.example.ticket.enums.EventType;
import com.example.ticket.enums.ShowtimeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {
    boolean existsByContract_RoomAndStatusNotAndStartTimeLessThanAndEndTimeGreaterThan(
            Room room, ShowtimeStatus excludedStatus, LocalDateTime endTime, LocalDateTime startTime);

    List<Showtime> findByContract_EventOrderByStartTimeAsc(Event event);
    List<Showtime> findByStatusAndEndTimeBefore(ShowtimeStatus status, LocalDateTime time);

    @Query("SELECT s FROM Showtime s " +
            "WHERE s.contract.event.status = 'PUBLISHED' " +
            "AND s.status = 'SCHEDULED' " +
            "AND (:type IS NULL OR s.contract.event.type = :type) " +
            "AND (:city IS NULL OR s.contract.venue.city = :city) " +
            "ORDER BY s.startTime ASC")
    List<Showtime> searchPublicShowTimes(@Param("type") EventType type, @Param("city") String city);

    List<Showtime> findByContract_VenueAndStatusAndStartTimeAfterOrderByStartTimeAsc(
            VenueProfile venue, ShowtimeStatus status, LocalDateTime now);
}