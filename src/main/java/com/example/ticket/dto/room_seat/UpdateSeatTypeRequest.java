package com.example.ticket.dto.room_seat;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateSeatTypeRequest {
    @NotEmpty(message = "Phải chọn ít nhất 1 ghế")
    private List<Long> seatIds;

    @NotNull(message = "Phải chọn loại ghế")
    private Long seatTypeId;
}