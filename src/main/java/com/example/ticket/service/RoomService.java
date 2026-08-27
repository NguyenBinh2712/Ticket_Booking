// service/RoomService.java
package com.example.ticket.service;

import com.example.ticket.dto.room_seat.*;
import com.example.ticket.entity.*;
import com.example.ticket.enums.ProfileStatus;
import com.example.ticket.enums.RoomStatus;
import com.example.ticket.exception.AppException;
import com.example.ticket.exception.ErrorCode;
import com.example.ticket.repository.*;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class RoomService {

    RoomRepository roomRepository;
    SeatRepository seatRepository;
    SeatTypeRepository seatTypeRepository;
    VenueProfileRepository venueProfileRepository;
    UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    private VenueProfile getCurrentVerifiedVenue() {
        User user = getCurrentUser();
        VenueProfile venue = venueProfileRepository.findByUser(user)
                .orElseThrow(() -> new AppException(ErrorCode.VENUE_PROFILE_NOT_FOUND));
        if (venue.getStatus() != ProfileStatus.VERIFIED) {
            throw new AppException(ErrorCode.VENUE_NOT_VERIFIED);
        }
        return venue;
    }

    // service/RoomService.java — thay hàm createRoom() bằng bản này, các hàm khác giữ nguyên

    public RoomResponse createRoom(RoomRequest request) {
        VenueProfile venue = getCurrentVerifiedVenue();
        if (roomRepository.existsByVenueAndName(venue, request.getName())) {
            throw new AppException(ErrorCode.ROOM_NAME_DUPLICATED);
        }

        SeatType defaultSeatType = seatTypeRepository.findById(request.getDefaultSeatTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.SEAT_TYPE_NOT_FOUND));

        // Khu VIP optional -- chỉ áp dụng nếu venue thực sự khai báo (vipRows/vipColumns > 0)
        boolean hasVipZone = request.getVipRows() != null && request.getVipRows() > 0
                && request.getVipColumns() != null && request.getVipColumns() > 0;

        SeatType vipSeatType = null;
        if (hasVipZone) {
            if (request.getVipSeatTypeId() == null) {
                throw new AppException(ErrorCode.SEAT_TYPE_NOT_FOUND);
            }
            vipSeatType = seatTypeRepository.findById(request.getVipSeatTypeId())
                    .orElseThrow(() -> new AppException(ErrorCode.SEAT_TYPE_NOT_FOUND));

            if (request.getVipRows() > request.getTotalRows() || request.getVipColumns() > request.getTotalColumns()) {
                throw new AppException(ErrorCode.INVALID_ROOM_LAYOUT);
            }
        }

        Room room = Room.builder()
                .venue(venue)
                .name(request.getName())
                .totalRows(request.getTotalRows())
                .totalColumns(request.getTotalColumns())
                .status(RoomStatus.ACTIVE)
                .build();
        room = roomRepository.save(room);

        int vipRowStart = -1, vipRowEnd = -1, vipColStart = -1, vipColEnd = -1;
        if (hasVipZone) {
            vipRowStart = (request.getTotalRows() - request.getVipRows()) / 2;
            vipRowEnd = vipRowStart + request.getVipRows();
            vipColStart = (request.getTotalColumns() - request.getVipColumns()) / 2 + 1;
            vipColEnd = vipColStart + request.getVipColumns();
        }

        List<Integer> aisleRows = request.getAisleRows() != null ? request.getAisleRows() : List.of();
        List<Integer> aisleColumns = request.getAisleColumns() != null ? request.getAisleColumns() : List.of();

        List<Seat> seats = new ArrayList<>();
        for (int r = 0; r < request.getTotalRows(); r++) {
            String rowLabel = toRowLabel(r);
            boolean wholeRowIsAisle = aisleRows.contains(r);

            for (int c = 1; c <= request.getTotalColumns(); c++) {
                boolean isAisle = wholeRowIsAisle || aisleColumns.contains(c);

                boolean isVip = hasVipZone
                        && r >= vipRowStart && r < vipRowEnd
                        && c >= vipColStart && c < vipColEnd;

                SeatType typeForThisSeat = isVip ? vipSeatType : defaultSeatType;

                seats.add(Seat.builder()
                        .room(room)
                        .seatRow(rowLabel)
                        .seatNumber(c)
                        .seatType(typeForThisSeat)
                        .active(!isAisle)   // lối đi -> active = false, không bán được
                        .seatSpan(1)
                        .build());
            }
        }
        seatRepository.saveAll(seats);

        return toResponse(room);
    }
    public List<RoomResponse> getMyRooms() {
        VenueProfile venue = getCurrentVerifiedVenue();
        return roomRepository.findByVenue(venue)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public RoomDetailResponse getRoomDetail(Long roomId) {
        VenueProfile venue = getCurrentVerifiedVenue();
        Room room = roomRepository.findByIdAndVenue(roomId, venue)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));

        List<SeatResponse> seats = seatRepository.findByRoomOrderBySeatRowAscSeatNumberAsc(room)
                .stream().map(this::toSeatResponse).collect(Collectors.toList());

        return RoomDetailResponse.builder()
                .id(room.getId())
                .name(room.getName())
                .totalRows(room.getTotalRows())
                .totalColumns(room.getTotalColumns())
                .seats(seats)
                .build();
    }

    public void updateRoomStatus(Long roomId, RoomStatus status) {
        VenueProfile venue = getCurrentVerifiedVenue();
        Room room = roomRepository.findByIdAndVenue(roomId, venue)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
        room.setStatus(status);
        roomRepository.save(room);
    }

    public void updateSeatType(Long roomId, UpdateSeatTypeRequest request) {
        VenueProfile venue = getCurrentVerifiedVenue();
        Room room = roomRepository.findByIdAndVenue(roomId, venue)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));

        SeatType seatType = seatTypeRepository.findById(request.getSeatTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.SEAT_TYPE_NOT_FOUND));

        List<Seat> seats = seatRepository.findAllById(request.getSeatIds());
        for (Seat seat : seats) {
            if (!seat.getRoom().getId().equals(room.getId())) {
                throw new AppException(ErrorCode.SEAT_NOT_FOUND);
            }
            seat.setSeatType(seatType);
        }
        seatRepository.saveAll(seats);
    }

    public void toggleSeats(Long roomId, ToggleSeatRequest request) {
        VenueProfile venue = getCurrentVerifiedVenue();
        Room room = roomRepository.findByIdAndVenue(roomId, venue)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));

        List<Seat> seats = seatRepository.findAllById(request.getSeatIds());
        for (Seat seat : seats) {
            if (!seat.getRoom().getId().equals(room.getId())) {
                throw new AppException(ErrorCode.SEAT_NOT_FOUND);
            }
            seat.setActive(request.getActive());
        }
        seatRepository.saveAll(seats);
    }

    public void mergeCoupleSeat(Long roomId, MergeCoupleSeatRequest request) {
        VenueProfile venue = getCurrentVerifiedVenue();
        Room room = roomRepository.findByIdAndVenue(roomId, venue)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));

        Seat base = seatRepository.findById(request.getBaseSeatId())
                .orElseThrow(() -> new AppException(ErrorCode.SEAT_NOT_FOUND));
        Seat merged = seatRepository.findById(request.getMergedSeatId())
                .orElseThrow(() -> new AppException(ErrorCode.SEAT_NOT_FOUND));

        boolean sameRoom = base.getRoom().getId().equals(room.getId()) && merged.getRoom().getId().equals(room.getId());
        boolean sameRow = base.getSeatRow().equals(merged.getSeatRow());
        boolean adjacent = merged.getSeatNumber().equals(base.getSeatNumber() + base.getSeatSpan());
        boolean bothEligible = base.getActive() && merged.getActive() && base.getSeatSpan() == 1;

        if (!sameRoom || !sameRow || !adjacent || !bothEligible) {
            throw new AppException(ErrorCode.SEAT_MERGE_INVALID);
        }

        SeatType coupleType = seatTypeRepository.findById(request.getSeatTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.SEAT_TYPE_NOT_FOUND));

        base.setSeatSpan(2);
        base.setSeatType(coupleType);
        merged.setActive(false);

        seatRepository.saveAll(List.of(base, merged));
    }

    public void splitCoupleSeat(Long roomId, Long baseSeatId, Long defaultSeatTypeId) {
        VenueProfile venue = getCurrentVerifiedVenue();
        Room room = roomRepository.findByIdAndVenue(roomId, venue)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));

        Seat base = seatRepository.findById(baseSeatId)
                .orElseThrow(() -> new AppException(ErrorCode.SEAT_NOT_FOUND));
        if (!base.getRoom().getId().equals(room.getId()) || base.getSeatSpan() != 2) {
            throw new AppException(ErrorCode.SEAT_MERGE_INVALID);
        }

        SeatType defaultType = seatTypeRepository.findById(defaultSeatTypeId)
                .orElseThrow(() -> new AppException(ErrorCode.SEAT_TYPE_NOT_FOUND));

        Seat mergedBack = seatRepository.findByRoomOrderBySeatRowAscSeatNumberAsc(room).stream()
                .filter(s -> s.getSeatRow().equals(base.getSeatRow())
                        && s.getSeatNumber().equals(base.getSeatNumber() + 1))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.SEAT_NOT_FOUND));

        base.setSeatSpan(1);
        base.setSeatType(defaultType);
        mergedBack.setActive(true);
        mergedBack.setSeatType(defaultType);

        seatRepository.saveAll(List.of(base, mergedBack));
    }

    private String toRowLabel(int index) {
        StringBuilder sb = new StringBuilder();
        int n = index;
        do {
            sb.insert(0, (char) ('A' + (n % 26)));
            n = n / 26 - 1;
        } while (n >= 0);
        return sb.toString();
    }

    private RoomResponse toResponse(Room room) {
        return RoomResponse.builder()
                .id(room.getId())
                .name(room.getName())
                .totalRows(room.getTotalRows())
                .totalColumns(room.getTotalColumns())
                .totalSeats(room.getTotalRows() * room.getTotalColumns())
                .status(room.getStatus())
                .build();
    }

    private SeatResponse toSeatResponse(Seat seat) {
        return SeatResponse.builder()
                .id(seat.getId())
                .seatRow(seat.getSeatRow())
                .seatNumber(seat.getSeatNumber())
                .seatTypeId(seat.getSeatType().getId())
                .seatTypeName(seat.getSeatType().getName())
                .extraPrice(seat.getSeatType().getExtraPrice().toString())
                .active(seat.getActive())
                .seatSpan(seat.getSeatSpan())
                .build();
    }
}