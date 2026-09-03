package com.example.ticket.service;

import com.example.ticket.constant.RedisKey;
import com.example.ticket.dto.booking.HoldSeatRequest;
import com.example.ticket.dto.booking.HoldSeatResponse;
import com.example.ticket.entity.*;
import com.example.ticket.enums.RoomStatus;
import com.example.ticket.enums.ShowtimeStatus;
import com.example.ticket.exception.AppException;
import com.example.ticket.exception.ErrorCode;
import com.example.ticket.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SeatHoldService {

    private static final long HOLD_SECONDS = 600; // 10 phút

    StringRedisTemplate redisTemplate;
    ShowtimeRepository showtimeRepository;
    SeatRepository seatRepository;
    BookingSeatRepository bookingSeatRepository;
    UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    public HoldSeatResponse holdSeats(HoldSeatRequest request) {
        User user = getCurrentUser();
        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new AppException(ErrorCode.SHOWTIME_NOT_FOUND));
        if (showtime.getStatus() != ShowtimeStatus.SCHEDULED) {
            throw new AppException(ErrorCode.SHOWTIME_NOT_AVAILABLE);
        }
        if (showtime.getStartTime().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.SHOWTIME_NOT_AVAILABLE);
        }

        Room room = showtime.getContract().getRoom();
        if (room.getStatus() != RoomStatus.ACTIVE) {
            throw new AppException(ErrorCode.ROOM_IN_MAINTENANCE);
        }
        List<Long> acquiredKeys = new ArrayList<>();

        try {
            for (Long seatId : request.getSeatIds()) {
                Seat seat = seatRepository.findById(seatId)
                        .orElseThrow(() -> new AppException(ErrorCode.SEAT_NOT_FOUND));
                if (!seat.getRoom().getId().equals(room.getId())) {
                    throw new AppException(ErrorCode.SEAT_NOT_IN_ROOM);
                }
                if (!seat.getActive()) {
                    throw new AppException(ErrorCode.SEAT_INACTIVE);
                }
                // Ghế đã có người mua thật (nằm trong DB) -> chặn ngay, không cần chờ tới bước confirm
                if (bookingSeatRepository.existsByShowtimeIdAndSeat_Id(showtime.getId(), seatId)) {
                    throw new AppException(ErrorCode.SEAT_ALREADY_BOOKED);
                }

                String key = RedisKey.SEAT_HOLD + showtime.getId() + ":" + seatId;
                Boolean acquired = redisTemplate.opsForValue()
                        .setIfAbsent(key, user.getId().toString(), Duration.ofSeconds(HOLD_SECONDS));

                if (Boolean.FALSE.equals(acquired)) {
                    throw new AppException(ErrorCode.SEAT_ALREADY_HELD);
                }
                acquiredKeys.add(seatId);
            }
        } catch (AppException e) {
            // 1 ghế trong danh sách thất bại -> nhả hết các ghế đã giữ được trước đó (all-or-nothing)
            for (Long seatId : acquiredKeys) {
                redisTemplate.delete(RedisKey.SEAT_HOLD + showtime.getId() + ":" + seatId);
            }
            throw e;
        }

        return HoldSeatResponse.builder()
                .showtimeId(showtime.getId())
                .heldSeatIds(request.getSeatIds())
                .expiresAt(Instant.now().plusSeconds(HOLD_SECONDS))
                .build();
    }

    public void releaseSeats(Long showtimeId, List<Long> seatIds) {
        User user = getCurrentUser();
        for (Long seatId : seatIds) {
            String key = RedisKey.SEAT_HOLD + showtimeId + ":" + seatId;
            String heldBy = redisTemplate.opsForValue().get(key);
            // Chỉ nhả ghế do chính mình giữ -- không cho user khác vô tình xóa hold của người khác
            if (heldBy != null && heldBy.equals(user.getId().toString())) {
                redisTemplate.delete(key);
            }
        }
    }

    // Dùng nội bộ bởi BookingService -- xác nhận toàn bộ ghế đang được đúng user này giữ
    public void verifyHeldByCurrentUser(Long showtimeId, List<Long> seatIds) {
        User user = getCurrentUser();
        for (Long seatId : seatIds) {
            String key = RedisKey.SEAT_HOLD + showtimeId + ":" + seatId;
            String heldBy = redisTemplate.opsForValue().get(key);
            if (heldBy == null) {
                throw new AppException(ErrorCode.SEAT_HOLD_NOT_FOUND);
            }
            if (!heldBy.equals(user.getId().toString())) {
                throw new AppException(ErrorCode.SEAT_HOLD_NOT_OWNED);
            }
        }
    }

    public void clearHold(Long showtimeId, List<Long> seatIds) {
        for (Long seatId : seatIds) {
            redisTemplate.delete(RedisKey.SEAT_HOLD + showtimeId + ":" + seatId);
        }
    }

    // Dùng để hiển thị sơ đồ ghế -- trả về userId đang giữ ghế đó, null nếu không ai giữ
    public String getHoldOwner(Long showtimeId, Long seatId) {
        return redisTemplate.opsForValue().get(RedisKey.SEAT_HOLD + showtimeId + ":" + seatId);
    }
}