package com.example.ticket.dto.dashboard;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EventRevenueSummary {
    private Long eventId;
    private String eventTitle;
    private BigDecimal totalRevenue;
    private Integer ticketCount;
}