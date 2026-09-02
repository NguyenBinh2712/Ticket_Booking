package com.example.ticket.entity;

import com.example.ticket.enums.ProfileStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "producer_profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProducerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "company_name", nullable = false, length = 150)
    private String companyName;

    @Column(name = "contact_email", length = 150)
    private String contactEmail;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ProfileStatus status = ProfileStatus.PENDING_VERIFICATION;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "reject_reason", columnDefinition = "TEXT")
    private String rejectReason;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}