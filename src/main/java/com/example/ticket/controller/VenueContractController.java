package com.example.ticket.controller;

import com.example.ticket.dto.ApiResponse;
import com.example.ticket.dto.contract.*;
import com.example.ticket.service.VenueContractService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/venues/me/contracts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('VENUE_OWNER')")
public class VenueContractController {

    VenueContractService venueContractService;

    @GetMapping("/pending")
    public ApiResponse<List<ContractResponse>> getPending() {
        ApiResponse<List<ContractResponse>> response = new ApiResponse<>();
        response.setResult(venueContractService.getMyPendingContracts());
        return response;
    }

    @PutMapping("/{contractId}/accept")
    public ApiResponse<Void> accept(@PathVariable Long contractId, @RequestBody @Valid ContractAcceptRequest request) {
        venueContractService.acceptContract(contractId, request);
        ApiResponse<Void> response = new ApiResponse<>();
        response.setMessage("Đã chấp nhận hợp đồng, suất chiếu đã được tạo");
        return response;
    }

    @PutMapping("/{contractId}/reject")
    public ApiResponse<Void> reject(@PathVariable Long contractId, @RequestBody @Valid ContractRejectRequest request) {
        venueContractService.rejectContract(contractId, request);
        ApiResponse<Void> response = new ApiResponse<>();
        response.setMessage("Đã từ chối hợp đồng");
        return response;
    }
}