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

        if (revenueTransactionRepository
                .findByBooking(booking)
                .isPresent()) {
            return;
        }
        EventVenueContract contract =
                booking.getShowtime().getContract();
        BigDecimal total = booking.getTotalPrice();
        BigDecimal producerPercent =
                contract.getProducerSharePercent();
        BigDecimal venuePercent =
                contract.getVenueSharePercent();
        BigDecimal adminPercent =
                contract.getAdminCommissionPercent();
        BigDecimal totalPercent = producerPercent.add(venuePercent).add(adminPercent);
        if (totalPercent.compareTo(BigDecimal.valueOf(100)) != 0) {
            throw new AppException(ErrorCode.INVALID_REVENUE_SPLIT);
        }
        BigDecimal producerAmount = total.multiply(producerPercent).divide(
                BigDecimal.valueOf(100),
                2,
                RoundingMode.HALF_UP
        );
        BigDecimal venueAmount = total.multiply(venuePercent).divide(
                BigDecimal.valueOf(100),
                2,
                RoundingMode.HALF_UP
                );
        BigDecimal adminAmount = total.multiply(adminPercent).divide(
                BigDecimal.valueOf(100),
                2,
                RoundingMode.HALF_UP
                );
        RevenueTransaction transaction =
                RevenueTransaction.builder()
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

    // Dùng khi hủy vé/hoàn tiền  -> đảo ngược, không xóa (giữ audit trail)
    public void reverseForBooking(Booking booking) {
        RevenueTransaction transaction = revenueTransactionRepository.findByBooking(booking)
                .orElseThrow(() -> new AppException(ErrorCode.REVENUE_TRANSACTION_NOT_FOUND));
        if (transaction.getStatus() == TransactionStatus.SETTLED || transaction.getSettlement() != null) {
            // không cho hoàn tự động
            throw new AppException(ErrorCode.TRANSACTION_ALREADY_SETTLED);
        }
        transaction.setStatus(TransactionStatus.REVERSED);
        revenueTransactionRepository.save(transaction);
    }
}