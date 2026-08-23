package com.example.ticket.dto.room_seat;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RoomDetailResponse {
    private Long id;
    private String name;
    private Integer totalRows;
    private Integer totalColumns;
    private List<SeatResponse> seats;
}