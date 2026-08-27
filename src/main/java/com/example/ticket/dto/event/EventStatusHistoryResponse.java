package com.example.ticket.dto.event;

import com.example.ticket.enums.EventStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EventStatusHistoryResponse {
    private EventStatus fromStatus;
    private EventStatus toStatus;
    private String changedByEmail;
    private String note;
    private LocalDateTime createdAt;
}