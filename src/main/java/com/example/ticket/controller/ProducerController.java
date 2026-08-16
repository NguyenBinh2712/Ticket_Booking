package com.example.ticket.controller;

import com.example.ticket.dto.ApiResponse;
import com.example.ticket.dto.profile_business_partner.ProducerProfileRequest;
import com.example.ticket.dto.profile_business_partner.ProfileResponse;
import com.example.ticket.service.ProducerProfileService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/producers")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProducerController {

    ProducerProfileService producerProfileService;

    @PreAuthorize("hasRole('PRODUCER')")
    @PostMapping("/me/profile")
    public ApiResponse<Void> createProfile(@RequestBody @Valid ProducerProfileRequest request) {
        producerProfileService.createProfile(request);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Gửi hồ sơ thành công, chờ Admin duyệt");
        return apiResponse;
    }

    @PreAuthorize("hasRole('PRODUCER')")
    @PutMapping("/me/profile")
    public ApiResponse<Void> updateProfile(@RequestBody @Valid ProducerProfileRequest request) {
        producerProfileService.updateProfile(request);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Cập nhật hồ sơ thành công");
        return apiResponse;
    }

    @GetMapping("/me/profile")
    public ApiResponse<ProfileResponse> getMyProfile() {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setResult(producerProfileService.getMyProfile());
        return apiResponse;
    }

    @GetMapping("/{userId}/profile")
    public ApiResponse<ProfileResponse> getProductProfile(@PathVariable Long userId) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setResult(producerProfileService.getProductProfile(userId));
        return apiResponse;
    }
}