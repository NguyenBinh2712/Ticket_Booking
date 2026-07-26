package com.example.ticket.repository;

import com.example.ticket.entity.RefreshToken;
import com.example.ticket.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {


        Optional<RefreshToken> findByTokenHash(String tokenHash);

        List<RefreshToken> findByUser(User user);
        void deleteByUser(User user);
}
