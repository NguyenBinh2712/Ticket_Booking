package com.example.ticket.repository;

import com.example.ticket.entity.Otp;
import com.example.ticket.entity.User;
import com.example.ticket.enums.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp,Long> {
    Optional<Otp> findByUserAndOtpAndType(User user, String otp, OtpType type);
    void deleteByUserAndType(User user, OtpType type);
}
