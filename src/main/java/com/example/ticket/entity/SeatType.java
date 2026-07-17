package com.example.ticket.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "seat_types")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SeatType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String name; // Thường, VIP, Couple

    @Column(name = "extra_price", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal extraPrice = BigDecimal.ZERO;
}