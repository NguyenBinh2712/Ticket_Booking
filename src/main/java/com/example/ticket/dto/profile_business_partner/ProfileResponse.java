package com.example.ticket.dto.profile_business_partner;

import com.example.ticket.enums.ProfileStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProfileResponse {
    private Long id;
    private Long userId;
    private String userEmail;
    private String name;
    private String description;
    private ProfileStatus status;
    private LocalDateTime createdAt;
}