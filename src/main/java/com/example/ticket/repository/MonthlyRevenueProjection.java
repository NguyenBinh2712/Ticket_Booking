package com.example.ticket.repository;

import java.math.BigDecimal;

public interface MonthlyRevenueProjection {
    String getMonth();
    BigDecimal getTotal();
}