package com.example.ticket.constant;

public class RedisKey {
    private RedisKey() {}

    public static final String OTP_COOLDOWN = "otp:cooldown:";
    public static final String OTP_COUNT = "otp:count:";

    public static final String SEAT_HOLD = "seat_hold:";
}
