package com.example.ticket.dto.event;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EventReviewRequest {
    @NotNull(message = "Phải chọn duyệt hoặc từ chối")
    private Boolean approve;

    private String note;
}