package com.example.ticket.dto.event;

import com.example.ticket.enums.ShowtimeStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PublicShowtimeResponse {
    private Long id;
    private Long eventId;
    private String eventTitle;
    private String eventPosterUrl;
    private Long venueId;
    private String venueName;
    private String venueCity;
    private String venueAddress;
    private Long roomId;
    private String roomName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal ticketPrice;
    private ShowtimeStatus status;
}