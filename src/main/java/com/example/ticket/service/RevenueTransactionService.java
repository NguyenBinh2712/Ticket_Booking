package com.example.ticket.service;

import com.example.ticket.entity.Booking;
import com.example.ticket.entity.EventVenueContract;
import com.example.ticket.entity.RevenueTransaction;
import com.example.ticket.enums.TransactionStatus;
import com.example.ticket.exception.AppException;
import com.example.ticket.exception.ErrorCode;
import com.example.ticket.repository.RevenueTransactionRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class RevenueTransactionService {

    RevenueTransactionRepository revenueTransactionRepository;

    public void createForBooking(Booking booking) {
        if (revenueTransactionRepository.findByBooking(booking).isPresent()) {
            return; // chống tạo trùng nếu callback cổng thanh toán gọi lại nhiều lần
        }

        EventVenueContract contract = booking.getShowtime().getContract();
        BigDecimal total = booking.getTotalPrice();

        // Cách A đã chốt: adminCommissionPercent (2.5%) khấu trừ trên TỔNG doanh thu, lấy từ MỖI BÊN
        BigDecimal commissionCut = total
                .multiply(contract.getAdminCommissionPercent())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal producerAmount = total
                .multiply(contract.getProducerSharePercent())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                .subtract(commissionCut);

        BigDecimal venueAmount = total
                .multiply(contract.getVenueSharePercent())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                .subtract(commissionCut);

        BigDecimal adminAmount = commissionCut.multiply(BigDecimal.valueOf(2));

        RevenueTransaction transaction = RevenueTransaction.builder()
                .booking(booking)
                .contract(contract)
                .totalAmount(total)
                .producerAmount(producerAmount)
                .venueAmount(venueAmount)
                .adminAmount(adminAmount)
                .status(TransactionStatus.PENDING)
                .build();

        revenueTransactionRepository.save(transaction);
    }

    // Dùng khi hủy vé/hoàn tiền TRƯỚC khi settle -- đảo ngược, không xóa (giữ audit trail)
    public void reverseForBooking(Booking booking) {
        RevenueTransaction transaction = revenueTransactionRepository.findByBooking(booking)
                .orElseThrow(() -> new AppException(ErrorCode.REVENUE_TRANSACTION_NOT_FOUND));

        if (transaction.getStatus() == TransactionStatus.SETTLED) {
            throw new AppException(ErrorCode.TRANSACTION_ALREADY_SETTLED);
        }

        transaction.setStatus(TransactionStatus.REVERSED);
        revenueTransactionRepository.save(transaction);
    }
}