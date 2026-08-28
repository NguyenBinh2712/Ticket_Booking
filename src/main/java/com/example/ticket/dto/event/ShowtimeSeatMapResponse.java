package com.example.ticket.dto.event;

import com.example.ticket.dto.room_seat.SeatResponse;
import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ShowtimeSeatMapResponse {
    private Long showtimeId;
    private Long roomId;
    private String roomName;
    private Integer totalRows;
    private Integer totalColumns;
    private List<SeatResponse> seats;
}