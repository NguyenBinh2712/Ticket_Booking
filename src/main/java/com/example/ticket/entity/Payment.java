package com.example.ticket.entity;

import com.example.ticket.enums.PaymentMethod;
import com.example.ticket.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod method;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "txn_ref", nullable = false, unique = true, length = 100)
    private String txnRef; // mã đơn hàng tự sinh, gửi cho cổng thanh toán

    @Column(name = "gateway_transaction_no", length = 100)
    private String gatewayTransactionNo; // mã giao dịch cổng trả về

    @Column(name = "response_code", length = 10)
    private String responseCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}