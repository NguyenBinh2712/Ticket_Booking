package com.example.ticket.repository;

import com.example.ticket.entity.ProducerProfile;
import com.example.ticket.entity.User;
import com.example.ticket.enums.ProfileStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProducerProfileRepository  extends JpaRepository<ProducerProfile,Long> {
    Optional<ProducerProfile> findByUser(User user);
    boolean existsByUser(User user);
    List<ProducerProfile> findByStatus(ProfileStatus status);
}
