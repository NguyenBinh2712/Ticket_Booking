package com.example.ticket.dto.dashboard;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminDashboardResponse {
    private BigDecimal totalRevenue;
    private BigDecimal totalCommissionCollected;
    private Long totalTransactions;
    private Long activeEventCount;
    private Long verifiedProducerCount;
    private Long verifiedVenueCount;
    private List<MonthlyRevenuePoint> monthlyRevenue;
}