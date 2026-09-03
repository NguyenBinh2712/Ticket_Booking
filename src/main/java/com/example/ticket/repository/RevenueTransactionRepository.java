package com.example.ticket.repository;

import com.example.ticket.entity.*;
import com.example.ticket.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RevenueTransactionRepository extends JpaRepository<RevenueTransaction, Long> {

    Optional<RevenueTransaction> findByBooking(Booking booking);

    List<RevenueTransaction> findByStatus(TransactionStatus status);

    List<RevenueTransaction> findByContract_Event_Producer(
            ProducerProfile producer
    );

    List<RevenueTransaction> findByContract_Venue(
            VenueProfile venue
    );

    @Query("""
        SELECT rt
        FROM RevenueTransaction rt
        WHERE rt.status = com.example.ticket.enums.TransactionStatus.PENDING
          AND rt.settlement IS NULL
          AND rt.booking.payment.paidAt >= :from
          AND rt.booking.payment.paidAt < :to
        """)
    List<RevenueTransaction> findUnsettledInPeriod(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    List<RevenueTransaction> findBySettlement(Settlement settlement);

    @Query("""
        SELECT COALESCE(SUM(rt.totalAmount), 0)
        FROM RevenueTransaction rt
        WHERE rt.status != com.example.ticket.enums.TransactionStatus.REVERSED
        """)
    BigDecimal sumTotalRevenue();

    @Query("""
        SELECT COALESCE(SUM(rt.adminAmount), 0)
        FROM RevenueTransaction rt
        WHERE rt.status != com.example.ticket.enums.TransactionStatus.REVERSED
        """)
    BigDecimal sumAdminCommission();

    long countByStatusNot(TransactionStatus status);

    @Query(
            value = """
                SELECT DATE_FORMAT(created_at, '%Y-%m') as month,
                       SUM(total_amount) as total
                FROM revenue_transactions
                WHERE status != 'REVERSED'
                GROUP BY DATE_FORMAT(created_at, '%Y-%m')
                ORDER BY month
                """,
            nativeQuery = true
    )
    List<MonthlyRevenueProjection> getMonthlyRevenue();
}