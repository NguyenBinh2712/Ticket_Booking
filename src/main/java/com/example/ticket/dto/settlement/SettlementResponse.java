package com.example.ticket.dto.settlement;

import com.example.ticket.enums.PartnerType;
import com.example.ticket.enums.SettlementStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SettlementResponse {
    private Long id;
    private PartnerType partnerType;
    private Long partnerId;
    private String partnerName;
    private LocalDate periodFrom;
    private LocalDate periodTo;
    private BigDecimal totalAmount;
    private Integer transactionCount;
    private SettlementStatus status;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}