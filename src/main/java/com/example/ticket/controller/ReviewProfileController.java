package com.example.ticket.controller;

import com.example.ticket.dto.ApiResponse;
import com.example.ticket.dto.profile_business_partner.ProfileResponse;
import com.example.ticket.dto.profile_business_partner.ProfileReviewRequest;
import com.example.ticket.service.ReviewProfileService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/profiles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('ADMIN')")
public class ReviewProfileController {

    ReviewProfileService reviewProfileService;

    @GetMapping("/producers/pending")
    public ApiResponse<List<ProfileResponse>> getPendingProducers() {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setResult(reviewProfileService.getPendingProducers());
        return apiResponse;
    }

    @GetMapping("/venues/pending")
    public ApiResponse<List<ProfileResponse>> getPendingVenues() {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setResult(reviewProfileService.getPendingVenues());
        return apiResponse;
    }

    @PutMapping("/producers/{id}/review")
    public ApiResponse<Void> reviewProducer(
            @PathVariable Long id, @RequestBody @Valid ProfileReviewRequest request) {
        reviewProfileService.reviewProducer(id, request);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Đã xử lý hồ sơ producer");
        return apiResponse;
    }

    @PutMapping("/venues/{id}/review")
    public ApiResponse<Void> reviewVenue(
            @PathVariable Long id, @RequestBody @Valid ProfileReviewRequest request) {
        reviewProfileService.reviewVenue(id, request);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Đã xử lý hồ sơ venue");
        return apiResponse;
    }
}