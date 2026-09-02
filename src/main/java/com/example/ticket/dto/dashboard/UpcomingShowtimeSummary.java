package com.example.ticket.dto.dashboard;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UpcomingShowtimeSummary {
    private Long showtimeId;
    private String eventTitle;
    private String roomName;
    private LocalDateTime startTime;
}