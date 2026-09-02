package com.example.ticket.repository;

import com.example.ticket.entity.User;
import com.example.ticket.entity.VenueProfile;
import com.example.ticket.enums.ProfileStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VenueProfileRepository extends JpaRepository<VenueProfile,Long> {
    Optional<VenueProfile> findByUser(User user);
    boolean existsByUser(User user);
    List<VenueProfile> findByStatus(ProfileStatus status);
    long countByStatus(ProfileStatus status);
}
