package com.example.ticket.entity;

import com.example.ticket.enums.ContractStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_venue_contracts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EventVenueContract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private VenueProfile venue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room; // chọn cụ thể khi Venue accept

    @Column(name = "producer_share_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal producerSharePercent;

    @Column(name = "venue_share_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal venueSharePercent;

    @Column(name = "admin_commission_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal adminCommissionPercent;

    @Column(name = "ticket_base_price", precision = 10, scale = 2)
    private BigDecimal ticketBasePrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ContractStatus status = ContractStatus.PROPOSED;

    @Column(name = "reject_reason", columnDefinition = "TEXT")
    private String rejectReason;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    // Validate tổng % = 100 nên làm ở tầng Service trước khi persist,
    // không ràng buộc được bằng annotation JPA thông thường.
}