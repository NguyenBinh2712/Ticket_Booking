package com.example.ticket.dto.dashboard;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProducerDashboardResponse {
    private BigDecimal totalRevenue;
    private Integer totalTicketsSold;
    private Integer totalEvents;
    private List<EventRevenueSummary> revenueByEvent;
}