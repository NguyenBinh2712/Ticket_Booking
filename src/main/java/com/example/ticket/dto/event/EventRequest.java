package com.example.ticket.dto.event;

import com.example.ticket.enums.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EventRequest {
    @NotBlank(message = "Tên sự kiện không được để trống")
    private String title;
    @NotNull(message = "Phải chọn loại sự kiện")
    private EventType type;
    private String description;
    private String posterUrl;

    private BigDecimal producerSharePercent;
    // Metadata (phim: director/cast, concert: artists...) -> lưu Mongo
    private Map<String, Object> attributes;
}