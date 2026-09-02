package com.example.ticket.dto.settlement;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SettlementCreateRequest {
    @NotNull(message = "Phải chọn ngày bắt đầu")
    private LocalDate periodFrom;

    @NotNull(message = "Phải chọn ngày kết thúc")
    private LocalDate periodTo;
}