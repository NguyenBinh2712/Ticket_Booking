package com.example.ticket.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seats", uniqueConstraints = {
        @UniqueConstraint(name = "uq_seat_position", columnNames = {"room_id", "seat_row", "seat_number"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "seat_row", nullable = false, length = 5)
    private String seatRow;

    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_type_id", nullable = false)
    private SeatType seatType;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;   // false = ô trống/lối đi, không bán được

    @Column(name = "seat_span", nullable = false)
    @Builder.Default
    private Integer seatSpan = 1;    // 2 = ghế đôi, chiếm luôn ô liền kề cùng hàng

}