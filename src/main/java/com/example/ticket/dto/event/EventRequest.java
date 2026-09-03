// dto/event/EventRequest.java
package com.example.ticket.dto.event;

import com.example.ticket.enums.EventType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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

    @DecimalMin(value = "2.5", message = "% phải >= 2.5 ")
    @DecimalMax(value = "97.5", message = "% phải <= 97.5")
    private BigDecimal producerSharePercent;

    private Map<String, Object> attributes;
}