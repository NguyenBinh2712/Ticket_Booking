package com.example.ticket.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Map;

@Document(collection = "event_details")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EventDetail {

    @Id
    private String id;

    private Long eventId;
    private String type; // MOVIE, CONCERT, WORKSHOP...

    private Map<String, Object> attributes;
}