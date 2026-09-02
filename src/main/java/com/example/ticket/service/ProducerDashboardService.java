package com.example.ticket.service;

import com.example.ticket.dto.dashboard.EventRevenueSummary;
import com.example.ticket.dto.dashboard.ProducerDashboardResponse;
import com.example.ticket.entity.*;
import com.example.ticket.exception.AppException;
import com.example.ticket.exception.ErrorCode;
import com.example.ticket.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProducerDashboardService {

    RevenueTransactionRepository revenueTransactionRepository;
    ProducerProfileRepository producerProfileRepository;
    EventRepository eventRepository;
    UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    public ProducerDashboardResponse getDashboard() {
        ProducerProfile producer = producerProfileRepository.findByUser(getCurrentUser())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCER_PROFILE_NOT_FOUND));

        List<RevenueTransaction> transactions = revenueTransactionRepository
                .findByContract_Event_Producer(producer).stream()
                .filter(rt -> rt.getStatus() != com.example.ticket.enums.TransactionStatus.REVERSED)
                .collect(Collectors.toList());

        BigDecimal totalRevenue = transactions.stream()
                .map(RevenueTransaction::getProducerAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Event, List<RevenueTransaction>> byEvent = transactions.stream()
                .collect(Collectors.groupingBy(rt -> rt.getContract().getEvent()));

        List<EventRevenueSummary> revenueByEvent = byEvent.entrySet().stream()
                .map(entry -> EventRevenueSummary.builder()
                        .eventId(entry.getKey().getId())
                        .eventTitle(entry.getKey().getTitle())
                        .totalRevenue(entry.getValue().stream()
                                .map(RevenueTransaction::getProducerAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add))
                        .ticketCount(entry.getValue().size())
                        .build())
                .collect(Collectors.toList());

        long totalEvents = eventRepository.findByProducer(producer).size();

        return ProducerDashboardResponse.builder()
                .totalRevenue(totalRevenue)
                .totalTicketsSold(transactions.size())
                .totalEvents((int) totalEvents)
                .revenueByEvent(revenueByEvent)
                .build();
    }
}