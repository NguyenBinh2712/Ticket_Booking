package com.example.ticket.dto.profile_business_partner;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProfileReviewRequest {
    @NotNull(message = "Phải chọn duyệt hoặc từ chối")
    private Boolean approve;

    private String rejectReason;
}