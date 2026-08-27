package com.example.ticket.dto.contract;

import com.example.ticket.enums.ContractStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ContractResponse {
    private Long id;
    private Long eventId;
    private String eventTitle;
    private Long venueId;
    private String venueName;
    private Long roomId;
    private String roomName;
    private BigDecimal producerSharePercent;
    private BigDecimal venueSharePercent;
    private BigDecimal adminCommissionPercent;
    private BigDecimal ticketBasePrice;
    private ContractStatus status;
    private String rejectReason;
    private LocalDateTime createdAt;
}