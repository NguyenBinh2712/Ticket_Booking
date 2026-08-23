package com.example.ticket.dto.room_seat;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MergeCoupleSeatRequest {
    @NotNull(message = "Phải chọn ghế gốc (bên trái)")
    private Long baseSeatId;

    @NotNull(message = "Phải chọn ghế bị gộp (bên phải, liền kề)")
    private Long mergedSeatId;

    @NotNull(message = "Phải chọn loại ghế áp dụng cho ghế đôi")
    private Long seatTypeId;
}