package com.example.ticket.repository;

import com.example.ticket.entity.Room;
import com.example.ticket.entity.VenueProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room,Long> {
    List<Room> findByVenue(VenueProfile venue);
    Optional<Room> findByIdAndVenue(Long id, VenueProfile venue);
    boolean existsByVenueAndName(VenueProfile venue, String name);
}
