package com.example.ticket.service;

import com.example.ticket.entity.RefreshToken;
import com.example.ticket.entity.User;
import com.example.ticket.exception.AppException;
import com.example.ticket.exception.ErrorCode;
import com.example.ticket.repository.RefreshTokenRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class RefreshTokenService {
    final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-duration}")
    private long refreshDuration;

    public String createRefreshToken(User user){
        String token= UUID.randomUUID().toString();
        refreshTokenRepository.deleteByUser(user);
        RefreshToken refreshToken=RefreshToken.builder()
                .tokenHash(token)
                .user(user)
                .expiryDate(
                        Instant.now().plus(refreshDuration, ChronoUnit.DAYS)
                )
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        return token;
    }

    public RefreshToken verify(String token){
        RefreshToken refreshToken=refreshTokenRepository.findByTokenHash(token)
                .orElseThrow(()-> new AppException(ErrorCode.UNAUTHENTICATED));

        if(refreshToken.isRevoked()){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if(refreshToken.getExpiryDate().isBefore(Instant.now())){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return refreshToken;
    }

    public void revoked(String token){
        RefreshToken refreshToken=verify(token);
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

}
