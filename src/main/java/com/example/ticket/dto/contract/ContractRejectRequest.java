package com.example.ticket.dto.contract;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ContractRejectRequest {
    @NotBlank(message = "Phải nhập lý do từ chối")
    private String rejectReason;
}