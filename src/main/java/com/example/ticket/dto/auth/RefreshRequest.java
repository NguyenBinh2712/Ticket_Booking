package com.example.ticket.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RefreshRequest {
//    @NotBlank(message = "Token không được để trống")
    private String token;
}