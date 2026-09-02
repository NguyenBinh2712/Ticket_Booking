package com.example.ticket.controller;

import com.example.ticket.dto.ApiResponse;
import com.example.ticket.dto.dashboard.*;
import com.example.ticket.service.AdminDashboardService;
import com.example.ticket.service.ProducerDashboardService;
import com.example.ticket.service.VenueDashboardService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DashboardController {

    ProducerDashboardService producerDashboardService;
    VenueDashboardService venueDashboardService;
    AdminDashboardService adminDashboardService;

    @PreAuthorize("hasRole('PRODUCER')")
    @GetMapping("/producers/me/dashboard")
    public ApiResponse<ProducerDashboardResponse> producerDashboard() {
        ApiResponse<ProducerDashboardResponse> response = new ApiResponse<>();
        response.setResult(producerDashboardService.getDashboard());
        return response;
    }

    @PreAuthorize("hasRole('VENUE_OWNER')")
    @GetMapping("/venues/me/dashboard")
    public ApiResponse<VenueDashboardResponse> venueDashboard() {
        ApiResponse<VenueDashboardResponse> response = new ApiResponse<>();
        response.setResult(venueDashboardService.getDashboard());
        return response;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/dashboard")
    public ApiResponse<AdminDashboardResponse> adminDashboard() {
        ApiResponse<AdminDashboardResponse> response = new ApiResponse<>();
        response.setResult(adminDashboardService.getDashboard());
        return response;
    }
}