package com.example.ticket.dto.room_seat;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RoomRequest {
    @NotBlank(message = "Tên phòng không được để trống")
    private String name;

    @NotNull(message = "Số hàng ghế không được để trống")
    @Min(value = 1, message = "Số hàng phải lớn hơn 0")
    private Integer totalRows;

    @NotNull(message = "Số cột ghế không được để trống")
    @Min(value = 1, message = "Số cột phải lớn hơn 0")
    private Integer totalColumns;

    @NotNull(message = "Phải chọn loại ghế mặc định")
    private Long defaultSeatTypeId;
}