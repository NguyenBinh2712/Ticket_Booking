package com.example.ticket.dto.booking;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BookingConfirmRequest {
    @NotNull(message = "Phải chọn suất chiếu")
    private Long showtimeId;

    @NotEmpty(message = "Phải chọn ít nhất 1 ghế")
    private List<Long> seatIds;
}