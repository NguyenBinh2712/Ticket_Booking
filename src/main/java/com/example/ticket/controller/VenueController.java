package com.example.ticket.controller;

import com.example.ticket.dto.ApiResponse;
import com.example.ticket.dto.profile_business_partner.ProfileResponse;
import com.example.ticket.dto.profile_business_partner.VenueProfileRequest;
import com.example.ticket.service.VenueProfileService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/venues")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VenueController {

    VenueProfileService venueProfileService;

    @PreAuthorize("hasRole('VENUE_OWNER')")
    @PostMapping("/me/profile")
    public ApiResponse<Void> createProfile(@RequestBody @Valid VenueProfileRequest request) {
        venueProfileService.createProfile(request);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Gửi hồ sơ thành công, chờ Admin duyệt");
        return apiResponse;
    }

    @PreAuthorize("hasRole('VENUE_OWNER')")
    @PutMapping("/me/profile")
    public ApiResponse<Void> updateProfile(@RequestBody @Valid VenueProfileRequest request) {
        venueProfileService.updateProfile(request);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Cập nhật hồ sơ thành công");
        return apiResponse;
    }

    @GetMapping("/me/profile")
    public ApiResponse<ProfileResponse> getMyProfile() {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setResult(venueProfileService.getMyProfile());
        return apiResponse;
    }

    @GetMapping("/{userId}/profile")
    public ApiResponse<ProfileResponse> getProductProfile(@PathVariable Long userId) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setResult(venueProfileService.getVenueProfile(userId));
        return apiResponse;
    }
}