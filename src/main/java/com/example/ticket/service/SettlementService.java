package com.example.ticket.service;

import com.example.ticket.dto.settlement.SettlementCreateRequest;
import com.example.ticket.dto.settlement.SettlementResponse;
import com.example.ticket.entity.*;
import com.example.ticket.enums.PartnerType;
import com.example.ticket.enums.SettlementStatus;
import com.example.ticket.enums.TransactionStatus;
import com.example.ticket.exception.AppException;
import com.example.ticket.exception.ErrorCode;
import com.example.ticket.repository.RevenueTransactionRepository;
import com.example.ticket.repository.SettlementRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class SettlementService {

    SettlementRepository settlementRepository;
    RevenueTransactionRepository revenueTransactionRepository;

    // Gom RevenueTransaction PENDING trong kỳ thành các Settlement riêng theo từng Producer/Venue
    public List<SettlementResponse> createSettlements(SettlementCreateRequest request) {
        if (!request.getPeriodTo().isAfter(request.getPeriodFrom())) {
            throw new AppException(ErrorCode.SETTLEMENT_PERIOD_INVALID);
        }

        List<RevenueTransaction> transactions = revenueTransactionRepository
                .findUnsettledInPeriod(request.getPeriodFrom(), request.getPeriodTo());

        if (transactions.isEmpty()) {
            throw new AppException(ErrorCode.SETTLEMENT_NO_TRANSACTIONS);
        }

        List<Settlement> results = new java.util.ArrayList<>();

        // Nhóm theo Producer -- key là producerProfile.id
        Map<Long, List<RevenueTransaction>> byProducer = transactions.stream()
                .collect(Collectors.groupingBy(rt -> rt.getContract().getEvent().getProducer().getId()));
        for (Map.Entry<Long, List<RevenueTransaction>> entry : byProducer.entrySet()) {
            results.add(buildSettlement(PartnerType.PRODUCER, entry.getKey(), entry.getValue(),
                    RevenueTransaction::getProducerAmount, request));
        }

        // Nhóm theo Venue -- key là venueProfile.id
        Map<Long, List<RevenueTransaction>> byVenue = transactions.stream()
                .collect(Collectors.groupingBy(rt -> rt.getContract().getVenue().getId()));
        for (Map.Entry<Long, List<RevenueTransaction>> entry : byVenue.entrySet()) {
            results.add(buildSettlement(PartnerType.VENUE, entry.getKey(), entry.getValue(),
                    RevenueTransaction::getVenueAmount, request));
        }

        return results.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private Settlement buildSettlement(PartnerType type, Long partnerId, List<RevenueTransaction> txs,
                                       Function<RevenueTransaction, BigDecimal> amountExtractor,
                                       SettlementCreateRequest request) {
        BigDecimal total = txs.stream()
                .map(amountExtractor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Settlement settlement = Settlement.builder()
                .partnerType(type)
                .partnerId(partnerId)
                .periodFrom(request.getPeriodFrom())
                .periodTo(request.getPeriodTo())
                .totalAmount(total)
                .status(SettlementStatus.PENDING)
                .build();
        settlement = settlementRepository.save(settlement);

        // Gắn settlement vào từng transaction -- CHƯA đổi status sang SETTLED,
        // chỉ "khóa mềm" (không cho gom lại lần 2 nhờ điều kiện settlement IS NULL ở query trên)
        for (RevenueTransaction tx : txs) {
            tx.setSettlement(settlement);
        }
        // txs đã là entity quản lý bởi Hibernate (lấy từ repository trong cùng transaction) -> tự flush khi commit

        return settlement;
    }

    public List<SettlementResponse> getPendingSettlements() {
        return settlementRepository.findByStatus(SettlementStatus.PENDING)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // Admin xác nhận đã thực sự chuyển tiền -- ĐÂY mới là lúc khóa cứng transaction (SETTLED)
    public void markAsPaid(Long settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new AppException(ErrorCode.SETTLEMENT_NOT_FOUND));

        if (settlement.getStatus() == SettlementStatus.PAID) {
            throw new AppException(ErrorCode.SETTLEMENT_ALREADY_PAID);
        }

        settlement.setStatus(SettlementStatus.PAID);
        settlement.setPaidAt(LocalDateTime.now());
        settlementRepository.save(settlement);

        List<RevenueTransaction> transactions = revenueTransactionRepository.findBySettlement(settlement);
        for (RevenueTransaction tx : transactions) {
            tx.setStatus(TransactionStatus.SETTLED);
        }
        revenueTransactionRepository.saveAll(transactions);
    }

    private SettlementResponse toResponse(Settlement s) {
        int count = revenueTransactionRepository.findBySettlement(s).size();
        String partnerName = null; // để trống, FE tự tra theo partnerId nếu cần hiển thị tên -- tránh phụ thuộc chéo repository ở đây

        return SettlementResponse.builder()
                .id(s.getId())
                .partnerType(s.getPartnerType())
                .partnerId(s.getPartnerId())
                .partnerName(partnerName)
                .periodFrom(s.getPeriodFrom())
                .periodTo(s.getPeriodTo())
                .totalAmount(s.getTotalAmount())
                .transactionCount(count)
                .status(s.getStatus())
                .paidAt(s.getPaidAt())
                .createdAt(s.getCreatedAt())
                .build();
    }
}