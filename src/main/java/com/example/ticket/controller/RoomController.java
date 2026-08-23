package com.example.ticket.controller;

import com.example.ticket.dto.ApiResponse;
import com.example.ticket.dto.room_seat.*;
import com.example.ticket.enums.RoomStatus;
import com.example.ticket.service.RoomService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/venues/me/rooms")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('VENUE_OWNER')")
public class RoomController {

    RoomService roomService;

    @PostMapping
    public ApiResponse<RoomResponse> createRoom(@RequestBody @Valid RoomRequest request) {
        ApiResponse<RoomResponse> response = new ApiResponse<>();
        response.setResult(roomService.createRoom(request));
        response.setMessage("Tạo phòng thành công");
        return response;
    }

    @GetMapping
    public ApiResponse<List<RoomResponse>> getMyRooms() {
        ApiResponse<List<RoomResponse>> response = new ApiResponse<>();
        response.setResult(roomService.getMyRooms());
        return response;
    }

    @GetMapping("/{roomId}")
    public ApiResponse<RoomDetailResponse> getRoomDetail(@PathVariable Long roomId) {
        ApiResponse<RoomDetailResponse> response = new ApiResponse<>();
        response.setResult(roomService.getRoomDetail(roomId));
        return response;
    }

    @PutMapping("/{roomId}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long roomId, @RequestParam RoomStatus status) {
        roomService.updateRoomStatus(roomId, status);
        ApiResponse<Void> response = new ApiResponse<>();
        response.setMessage("Cập nhật trạng thái phòng thành công");
        return response;
    }

    @PutMapping("/{roomId}/seats/type")
    public ApiResponse<Void> updateSeatType(@PathVariable Long roomId, @RequestBody @Valid UpdateSeatTypeRequest request) {
        roomService.updateSeatType(roomId, request);
        ApiResponse<Void> response = new ApiResponse<>();
        response.setMessage("Cập nhật loại ghế thành công");
        return response;
    }

    @PutMapping("/{roomId}/seats/toggle")
    public ApiResponse<Void> toggleSeats(@PathVariable Long roomId, @RequestBody @Valid ToggleSeatRequest request) {
        roomService.toggleSeats(roomId, request);
        ApiResponse<Void> response = new ApiResponse<>();
        response.setMessage("Cập nhật trạng thái ghế thành công");
        return response;
    }

    @PutMapping("/{roomId}/seats/merge-couple")
    public ApiResponse<Void> mergeCoupleSeat(@PathVariable Long roomId, @RequestBody @Valid MergeCoupleSeatRequest request) {
        roomService.mergeCoupleSeat(roomId, request);
        ApiResponse<Void> response = new ApiResponse<>();
        response.setMessage("Gộp ghế đôi thành công");
        return response;
    }

    @PutMapping("/{roomId}/seats/{baseSeatId}/split")
    public ApiResponse<Void> splitCoupleSeat(
            @PathVariable Long roomId, @PathVariable Long baseSeatId, @RequestParam Long defaultSeatTypeId) {
        roomService.splitCoupleSeat(roomId, baseSeatId, defaultSeatTypeId);
        ApiResponse<Void> response = new ApiResponse<>();
        response.setMessage("Tách ghế đôi thành công");
        return response;
    }
}