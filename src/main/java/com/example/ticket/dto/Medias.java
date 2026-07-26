package com.example.ticket.dto;

import com.example.ticket.enums.MediaType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Medias {
    String url;
    String publicId;
    String thumbnail;
    Integer duration;
    MediaType mediaType;
    Integer sortOrder;
}
