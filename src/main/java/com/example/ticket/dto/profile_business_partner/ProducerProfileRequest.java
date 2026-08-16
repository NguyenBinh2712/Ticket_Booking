package com.example.ticket.dto.profile_business_partner;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProducerProfileRequest {
    @NotBlank(message = "Tên công ty không được để trống")
    private String companyName;

    private String contactEmail;
    private String contactPhone;
    private String description;
}