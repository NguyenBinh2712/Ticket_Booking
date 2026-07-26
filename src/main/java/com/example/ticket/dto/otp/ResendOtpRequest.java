package com.example.ticket.dto.otp;

import com.example.ticket.enums.OtpType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResendOtpRequest {
    String email;
    OtpType type;
}
