package com.example.ticket.dto.contract;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ContractAcceptRequest {
    @NotNull(message = "Phải chọn phòng")
    private Long roomId;

    @NotNull(message = "Phải nhập giờ bắt đầu")
    private LocalDateTime startTime;

    @NotNull(message = "Phải nhập giờ kết thúc")
    private LocalDateTime endTime;
}