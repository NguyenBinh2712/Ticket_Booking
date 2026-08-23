package com.example.ticket.dto.room_seat;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SeatResponse {
    private Long id;
    private String seatRow;
    private Integer seatNumber;
    private Long seatTypeId;
    private String seatTypeName;
    private String extraPrice;
    private Boolean active;
    private Integer seatSpan;
}