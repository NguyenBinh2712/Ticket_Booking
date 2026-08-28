package com.example.ticket.dto.booking;

import lombok.*;
import java.time.Instant;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HoldSeatResponse {
    private Long showtimeId;
    private List<Long> heldSeatIds;
    private Instant expiresAt;
}