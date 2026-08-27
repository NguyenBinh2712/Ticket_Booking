package com.example.ticket.dto.contract;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ContractCreateRequest {
    @NotNull(message = "Phải chọn Venue")
    private Long venueId;

    @NotNull(message = "Phải nhập giá vé cơ sở")
    private BigDecimal ticketBasePrice;
}