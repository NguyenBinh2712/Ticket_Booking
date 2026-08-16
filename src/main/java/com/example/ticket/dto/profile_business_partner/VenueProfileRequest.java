package com.example.ticket.dto.profile_business_partner;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VenueProfileRequest {
    @NotBlank(message = "Tên địa điểm không được để trống")
    private String venueName;

    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;

    @NotBlank(message = "Thành phố không được để trống")
    private String city;

    private Double areaSqm;
    private String description;
}