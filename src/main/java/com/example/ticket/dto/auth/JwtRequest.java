package com.example.ticket.dto.auth;

import com.example.ticket.enums.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtRequest {
    private Long userId;
    private String email;
    private Role role;

}
