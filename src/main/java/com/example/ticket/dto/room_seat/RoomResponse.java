package com.example.ticket.dto.room_seat;

import com.example.ticket.enums.RoomStatus;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RoomResponse {
    private Long id;
    private String name;
    private Integer totalRows;
    private Integer totalColumns;
    private Integer totalSeats;
    private RoomStatus status;
}