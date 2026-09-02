package com.example.ticket.service;

import com.example.ticket.dto.booking.BookingConfirmRequest;
import com.example.ticket.dto.booking.BookingResponse;
import com.example.ticket.entity.*;
import com.example.ticket.enums.BookingStatus;
import com.example.ticket.enums.ShowtimeStatus;
import com.example.ticket.exception.AppException;
import com.example.ticket.exception.ErrorCode;
import com.example.ticket.repository.*;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingService {

    BookingRepository bookingRepository;
    ShowtimeRepository showtimeRepository;
    SeatRepository seatRepository;
    UserRepository userRepository;
    SeatHoldService seatHoldService;
    PaymentConfirmationService paymentConfirmationService;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    @Transactional
    public BookingResponse confirmBooking(BookingConfirmRequest request) {
        User user = getCurrentUser();
        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new AppException(ErrorCode.SHOWTIME_NOT_FOUND));
        if (showtime.getStatus() != ShowtimeStatus.SCHEDULED) {
            throw new AppException(ErrorCode.SHOWTIME_NOT_AVAILABLE);
        }
        if (showtime.getStartTime().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.SHOWTIME_NOT_AVAILABLE);
        }
        List<Long> requestedSeatIds = request.getSeatIds();
        if (requestedSeatIds.stream().distinct().count() != requestedSeatIds.size()) {
            throw new AppException(ErrorCode.NO_SEATS_SELECTED);
        }

        seatHoldService.verifyHeldByCurrentUser(request.getShowtimeId(), requestedSeatIds);
        List<Seat> seats = seatRepository.findAllById(requestedSeatIds);
        if (seats.size() != requestedSeatIds.size()) {
            throw new AppException(ErrorCode.SEAT_NOT_FOUND);
        }
        // Bước 1: xác nhận đúng user này đang giữ hết các ghế yêu cầu (lớp bảo vệ ở Redis)
        seatHoldService.verifyHeldByCurrentUser(request.getShowtimeId(), request.getSeatIds());
        BigDecimal total = BigDecimal.ZERO;
        List<BookingSeat> bookingSeats = new ArrayList<>();

        Booking booking = Booking.builder()
                .customer(user)
                .showtime(showtime)
                .bookingCode(generateBookingCode())
                .status(BookingStatus.PENDING) // chờ thanh toán
                .paymentDeadline(LocalDateTime.now().plusMinutes(15))
                .build();

        for (Seat seat : seats) {
            // Giá = (giá vé suất chiếu + phụ thu loại ghế) x seatSpan (ghế đôi tính gấp 2 vì chiếm 2 vị trí)
            BigDecimal price = showtime.getTicketPrice()
                    .add(seat.getSeatType().getExtraPrice())
                    .multiply(BigDecimal.valueOf(seat.getSeatSpan()));
            total = total.add(price);

            bookingSeats.add(BookingSeat.builder()
                    .booking(booking)
                    .showtimeId(showtime.getId())
                    .seat(seat)
                    .price(price)
                    .build());
        }

        booking.setTotalPrice(total);
        booking.setBookingSeats(bookingSeats);

        try {
            // lớp bảo vệ CUỐI CÙNG, hoạt động độc lập ngay cả khi Redis có lỗi hay hold đã hết hạn
            bookingRepository.save(booking);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.SEAT_ALREADY_BOOKED);
        }

        // Ghế đã "chốt" trong DB -- không cần giữ ở Redis nữa, nhả ngay để tối ưu bộ nhớ
        seatHoldService.clearHold(request.getShowtimeId(), request.getSeatIds());

        return toResponse(booking, seats);
    }

    public List<BookingResponse> getMyBookings() {
        User user = getCurrentUser();
        return bookingRepository.findByCustomer(user).stream()
                .map(b -> toResponse(b, b.getBookingSeats().stream().map(BookingSeat::getSeat).collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    public BookingResponse getMyBookingDetail(Long bookingId) {
        User user = getCurrentUser();
        Booking booking = bookingRepository.findByIdAndCustomer(bookingId, user)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        return toResponse(booking, booking.getBookingSeats().stream().map(BookingSeat::getSeat).collect(Collectors.toList()));
    }

    private String generateBookingCode() {
        String code;
        do {
            code = "BK" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (bookingRepository.existsByBookingCode(code));
        return code;
    }

    private BookingResponse toResponse(Booking booking, List<Seat> seats) {
        Showtime showtime = booking.getShowtime();
        Event event = showtime.getContract().getEvent();
        VenueProfile venue = showtime.getContract().getVenue();
        Room room = showtime.getContract().getRoom();

        List<String> seatLabels = seats.stream()
                .map(s -> s.getSeatRow() + s.getSeatNumber())
                .collect(Collectors.toList());

        return BookingResponse.builder()
                .id(booking.getId())
                .bookingCode(booking.getBookingCode())
                .showtimeId(showtime.getId())
                .eventTitle(event.getTitle())
                .venueName(venue.getVenueName())
                .roomName(room != null ? room.getName() : null)
                .startTime(showtime.getStartTime())
                .seatLabels(seatLabels)
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}