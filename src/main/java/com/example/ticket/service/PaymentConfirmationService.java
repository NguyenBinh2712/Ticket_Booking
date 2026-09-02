package com.example.ticket.service;

import com.example.ticket.entity.Booking;
import com.example.ticket.entity.BookingSeat;
import com.example.ticket.entity.Payment;
import com.example.ticket.enums.BookingStatus;
import com.example.ticket.enums.PaymentStatus;
import com.example.ticket.exception.AppException;
import com.example.ticket.exception.ErrorCode;
import com.example.ticket.repository.BookingRepository;
import com.example.ticket.repository.BookingSeatRepository;
import com.example.ticket.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class PaymentConfirmationService {

    PaymentRepository paymentRepository;
    BookingRepository bookingRepository;
    BookingSeatRepository bookingSeatRepository;
    RevenueTransactionService revenueTransactionService;

    public boolean confirmSuccess(Payment payment, String gatewayTransactionNo, String responseCode) {
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return false;
        }
        // Chặn chuyển SUCCESS từ trạng thái không hợp lệ (VD: đã FAILED/REFUNDED trước đó)
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new AppException(ErrorCode.PAYMENT_ALREADY_PROCESSED);
        }

        payment.setGatewayTransactionNo(gatewayTransactionNo);
        payment.setResponseCode(responseCode);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        Booking booking = payment.getBooking();
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        revenueTransactionService.createForBooking(booking);
        return true;
    }

    // SỬA: thanh toán thất bại -> hủy Booking + xóa hết BookingSeat -> ghế nhả ra ngay lập tức
    public void markFailed(Payment payment, String responseCode) {
        payment.setResponseCode(responseCode);
        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);

        releaseBooking(payment.getBooking());
    }

    // Dùng chung cho cả markFailed() và job dọn booking hết hạn (bên dưới)
    public void releaseBooking(Booking booking) {
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            return; // đã thanh toán thành công rồi thì không được đụng vào
        }
        List<BookingSeat> seats = bookingSeatRepository.findByShowtimeId(booking.getShowtime().getId())
                .stream().filter(bs -> bs.getBooking().getId().equals(booking.getId())).toList();
        bookingSeatRepository.deleteAll(seats); // XÓA hẳn -> unique constraint (showtime_id, seat_id) rảnh ra
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }
}