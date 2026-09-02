package com.example.ticket.dto.dashboard;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MonthlyRevenuePoint {
    private String month;
    private BigDecimal total;
}