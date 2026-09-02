// controller/SettlementController.java
package com.example.ticket.controller;

import com.example.ticket.dto.ApiResponse;
import com.example.ticket.dto.settlement.SettlementCreateRequest;
import com.example.ticket.dto.settlement.SettlementResponse;
import com.example.ticket.service.SettlementService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/settlements")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('ADMIN')")
public class SettlementController {

    SettlementService settlementService;

    @PostMapping
    public ApiResponse<List<SettlementResponse>> create(@RequestBody @Valid SettlementCreateRequest request) {
        ApiResponse<List<SettlementResponse>> response = new ApiResponse<>();
        response.setResult(settlementService.createSettlements(request));
        response.setMessage("Đã tạo đợt đối soát, chờ xác nhận thanh toán");
        return response;
    }

    @GetMapping("/pending")
    public ApiResponse<List<SettlementResponse>> getPending() {
        ApiResponse<List<SettlementResponse>> response = new ApiResponse<>();
        response.setResult(settlementService.getPendingSettlements());
        return response;
    }

    @PutMapping("/{id}/mark-paid")
    public ApiResponse<Void> markPaid(@PathVariable Long id) {
        settlementService.markAsPaid(id);
        ApiResponse<Void> response = new ApiResponse<>();
        response.setMessage("Đã xác nhận thanh toán đợt đối soát");
        return response;
    }
}