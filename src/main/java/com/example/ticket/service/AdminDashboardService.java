package com.example.ticket.service;

import com.example.ticket.dto.dashboard.AdminDashboardResponse;
import com.example.ticket.dto.dashboard.MonthlyRevenuePoint;
import com.example.ticket.enums.EventStatus;
import com.example.ticket.enums.ProfileStatus;
import com.example.ticket.repository.EventRepository;
import com.example.ticket.repository.ProducerProfileRepository;
import com.example.ticket.repository.RevenueTransactionRepository;
import com.example.ticket.repository.VenueProfileRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminDashboardService {

    RevenueTransactionRepository revenueTransactionRepository;
    EventRepository eventRepository;
    ProducerProfileRepository producerProfileRepository;
    VenueProfileRepository venueProfileRepository;

    public AdminDashboardResponse getDashboard() {
        List<MonthlyRevenuePoint> monthlyRevenue = revenueTransactionRepository.getMonthlyRevenue().stream()
                .map(p -> MonthlyRevenuePoint.builder().month(p.getMonth()).total(p.getTotal()).build())
                .collect(Collectors.toList());

        long activeEvents = eventRepository.countByStatusIn(
                List.of(EventStatus.PUBLISHED, EventStatus.ONGOING));

        return AdminDashboardResponse.builder()
                .totalRevenue(revenueTransactionRepository.sumTotalRevenue())
                .totalCommissionCollected(revenueTransactionRepository.sumAdminCommission())
                .totalTransactions(revenueTransactionRepository.count())
                .activeEventCount(activeEvents)
                .verifiedProducerCount(producerProfileRepository.countByStatus(ProfileStatus.VERIFIED))
                .verifiedVenueCount(venueProfileRepository.countByStatus(ProfileStatus.VERIFIED))
                .monthlyRevenue(monthlyRevenue)
                .build();
    }
}