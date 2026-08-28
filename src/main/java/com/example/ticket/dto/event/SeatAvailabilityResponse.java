package com.example.ticket.dto.event;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SeatAvailabilityResponse {
    private Long seatId;
    private String seatRow;
    private Integer seatNumber;
    private String seatTypeName;
    private String extraPrice;
    private Integer seatSpan;
    private Boolean active;
    private String status; // AVAILABLE | HELD | BOOKED
}