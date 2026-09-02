package com.example.ticket.entity;

import com.example.ticket.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "revenue_transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RevenueTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private EventVenueContract contract;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "producer_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal producerAmount;

    @Column(name = "venue_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal venueAmount;

    @Column(name = "admin_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal adminAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id")
    private Settlement settlement;
    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}