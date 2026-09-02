package com.example.ticket.service;

import com.example.ticket.dto.dashboard.UpcomingShowtimeSummary;
import com.example.ticket.dto.dashboard.VenueDashboardResponse;
import com.example.ticket.entity.*;
import com.example.ticket.enums.RoomStatus;
import com.example.ticket.enums.ShowtimeStatus;
import com.example.ticket.exception.AppException;
import com.example.ticket.exception.ErrorCode;
import com.example.ticket.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VenueDashboardService {

    RevenueTransactionRepository revenueTransactionRepository;
    VenueProfileRepository venueProfileRepository;
    RoomRepository roomRepository;
    SeatRepository seatRepository;
    BookingSeatRepository bookingSeatRepository;
    ShowtimeRepository showtimeRepository;
    UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    public VenueDashboardResponse getDashboard() {
        VenueProfile venue = venueProfileRepository.findByUser(getCurrentUser())
                .orElseThrow(() -> new AppException(ErrorCode.VENUE_PROFILE_NOT_FOUND));

        BigDecimal totalRevenue = revenueTransactionRepository.findByContract_Venue(venue).stream()
                .filter(rt -> rt.getStatus() != com.example.ticket.enums.TransactionStatus.REVERSED)
                .map(RevenueTransaction::getVenueAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long activeRooms = roomRepository.findByVenue(venue).stream()
                .filter(r -> r.getStatus() == RoomStatus.ACTIVE).count();

        // Ước lượng đơn giản: tổng ghế đã bán (toàn thời gian) / tổng ghế khả dụng hiện có của venue
        long totalActiveSeats = seatRepository.countActiveSeatsByVenue(venue);
        long confirmedSeats = bookingSeatRepository.countConfirmedSeatsByVenue(venue);
        double occupancyRate = totalActiveSeats == 0 ? 0.0
                : Math.round((confirmedSeats * 10000.0 / totalActiveSeats)) / 100.0;

        List<UpcomingShowtimeSummary> upcoming = showtimeRepository
                .findByContract_VenueAndStatusAndStartTimeAfterOrderByStartTimeAsc(
                        venue, ShowtimeStatus.SCHEDULED, LocalDateTime.now())
                .stream()
                .map(s -> UpcomingShowtimeSummary.builder()
                        .showtimeId(s.getId())
                        .eventTitle(s.getContract().getEvent().getTitle())
                        .roomName(s.getContract().getRoom() != null ? s.getContract().getRoom().getName() : null)
                        .startTime(s.getStartTime())
                        .build())
                .collect(Collectors.toList());

        return VenueDashboardResponse.builder()
                .totalRevenue(totalRevenue)
                .totalRoomsActive((int) activeRooms)
                .occupancyRatePercent(occupancyRate)
                .upcomingShowtimes(upcoming)
                .build();
    }
}