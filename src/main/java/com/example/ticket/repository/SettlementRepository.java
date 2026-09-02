package com.example.ticket.repository;

import com.example.ticket.entity.Settlement;
import com.example.ticket.enums.PartnerType;
import com.example.ticket.enums.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    List<Settlement> findByStatus(SettlementStatus status);
    List<Settlement> findByPartnerTypeAndPartnerId(PartnerType partnerType, Long partnerId);
}