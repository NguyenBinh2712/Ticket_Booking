package com.example.ticket.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "notifications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

    @Id
    private String id;

    private Long userId;
    private String type;
    private String title;
    private String message;
    private Long relatedEntityId;

    @Builder.Default
    private Boolean isRead = false;

    private LocalDateTime createdAt;
}