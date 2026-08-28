package com.example.ticket.dto.booking;
import com.example.ticket.enums.BookingStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BookingResponse {
    private Long id;
    private String bookingCode;
    private Long showtimeId;
    private String eventTitle;
    private String venueName;
    private String roomName;
    private LocalDateTime startTime;
    private List<String> seatLabels;
    private BigDecimal totalPrice;
    private BookingStatus status;
    private LocalDateTime createdAt;
}