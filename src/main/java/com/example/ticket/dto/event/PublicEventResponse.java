package com.example.ticket.dto.event;

import com.example.ticket.enums.EventStatus;
import com.example.ticket.enums.EventType;
import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PublicEventResponse {
    private Long id;
    private String title;
    private EventType type;
    private String description;
    private String posterUrl;
    private EventStatus status;
    private BigDecimal avgRating;
    private Map<String, Object> attributes;
}