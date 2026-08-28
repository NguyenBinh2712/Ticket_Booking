package com.example.ticket.dto.revenue;

import com.example.ticket.enums.TransactionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RevenueTransactionResponse {
    private Long id;
    private String bookingCode;
    private String eventTitle;
    private BigDecimal totalAmount;
    private BigDecimal producerAmount;
    private BigDecimal venueAmount;
    private BigDecimal adminAmount;
    private TransactionStatus status;
    private LocalDateTime createdAt;
}