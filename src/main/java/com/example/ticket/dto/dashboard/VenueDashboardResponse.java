package com.example.ticket.dto.dashboard;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VenueDashboardResponse {
    private BigDecimal totalRevenue;
    private Integer totalRoomsActive;
    private Double occupancyRatePercent; // ước lượng: ghế đã bán / tổng ghế khả dụng của venue
    private List<UpcomingShowtimeSummary> upcomingShowtimes;
}