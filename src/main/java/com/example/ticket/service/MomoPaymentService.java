package com.example.ticket.service;

import com.example.ticket.entity.Booking;
import com.example.ticket.entity.Payment;
import com.example.ticket.enums.BookingStatus;
import com.example.ticket.enums.PaymentMethod;
import com.example.ticket.enums.PaymentStatus;
import com.example.ticket.exception.AppException;
import com.example.ticket.exception.ErrorCode;
import com.example.ticket.repository.BookingRepository;
import com.example.ticket.repository.PaymentRepository;
import com.example.ticket.util.MomoUtil;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Transactional
public class MomoPaymentService {

    private static final long BOOKING_PAYMENT_TIMEOUT_MINUTES = 15;
    private final RestTemplate restTemplate = new RestTemplate();

    BookingRepository bookingRepository;
    PaymentRepository paymentRepository;
    PaymentConfirmationService paymentConfirmationService;

    @Value("${momo.partner-code}")
    String partnerCode;
    @Value("${momo.access-key}")
    String accessKey;
    @Value("${momo.secret-key}")
    String secretKey;
    @Value("${momo.endpoint}")
    String endpoint;
    @Value("${momo.redirect-url}")
    String redirectUrl;
    @Value("${momo.ipn-url}")
    String ipnUrl;

    public String createPaymentUrl(Booking booking) {
        validateBookingPayable(booking);

        String requestId = UUID.randomUUID().toString();
        String orderId = "BK" + booking.getId() + "_" + System.currentTimeMillis();
        String amount = String.valueOf(booking.getTotalPrice().longValueExact());
        String orderInfo = "Thanh toan don ve " + booking.getBookingCode();
        String requestType = "captureWallet";
        String extraData = "";

        String rawSignature = "accessKey=" + accessKey +
                "&amount=" + amount +
                "&extraData=" + extraData +
                "&ipnUrl=" + ipnUrl +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + partnerCode +
                "&redirectUrl=" + redirectUrl +
                "&requestId=" + requestId +
                "&requestType=" + requestType;
        String signature = MomoUtil.hmacSHA256(secretKey, rawSignature);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("partnerCode", partnerCode);
        body.put("accessKey", accessKey);
        body.put("requestId", requestId);
        body.put("amount", amount);
        body.put("orderId", orderId);
        body.put("orderInfo", orderInfo);
        body.put("redirectUrl", redirectUrl);
        body.put("ipnUrl", ipnUrl);
        body.put("extraData", extraData);
        body.put("requestType", requestType);
        body.put("signature", signature);
        body.put("lang", "vi");

        Map<String, Object> response;
        try {
            response = restTemplate.postForObject(endpoint, body, Map.class);
        } catch (Exception e) {
            throw new AppException(ErrorCode.PAYMENT_GATEWAY_ERROR);
        }
        if (response == null || !"0".equals(String.valueOf(response.get("resultCode")))) {
            throw new AppException(ErrorCode.PAYMENT_GATEWAY_ERROR);
        }

        savePendingPayment(booking, orderId);
        return (String) response.get("payUrl");
    }

    public String handleIpn(Map<String, Object> params) {
        String orderId = String.valueOf(params.get("orderId"));
        Payment payment = paymentRepository.findByTxnRef(orderId).orElse(null);
        if (payment == null) {
            return "{\"resultCode\":1,\"message\":\"Order not found\"}";
        }

        String receivedSignature = String.valueOf(params.get("signature"));
        String rawSignature = "accessKey=" + accessKey +
                "&amount=" + params.get("amount") +
                "&extraData=" + params.getOrDefault("extraData", "") +
                "&message=" + params.get("message") +
                "&orderId=" + params.get("orderId") +
                "&orderInfo=" + params.get("orderInfo") +
                "&orderType=" + params.get("orderType") +
                "&partnerCode=" + params.get("partnerCode") +
                "&payType=" + params.get("payType") +
                "&requestId=" + params.get("requestId") +
                "&responseTime=" + params.get("responseTime") +
                "&resultCode=" + params.get("resultCode") +
                "&transId=" + params.get("transId");
        String computed = MomoUtil.hmacSHA256(secretKey, rawSignature);
        if (!computed.equals(receivedSignature)) {
            return "{\"resultCode\":1,\"message\":\"Invalid signature\"}";
        }

        long expectedAmount = payment.getAmount().longValueExact();
        long receivedAmount = Long.parseLong(String.valueOf(params.get("amount")));
        if (expectedAmount != receivedAmount) {
            return "{\"resultCode\":1,\"message\":\"Invalid amount\"}";
        }

        String resultCode = String.valueOf(params.get("resultCode"));
        if ("0".equals(resultCode)) {
            paymentConfirmationService.confirmSuccess(payment, String.valueOf(params.get("transId")), resultCode);
        } else {
            paymentConfirmationService.markFailed(payment, resultCode);
        }

        return "{\"resultCode\":0,\"message\":\"Confirm Success\"}";
    }

    private void validateBookingPayable(Booking booking) {
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            throw new AppException(ErrorCode.BOOKING_ALREADY_CONFIRMED);
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new AppException(ErrorCode.BOOKING_EXPIRED);
        }
        if (Duration.between(booking.getCreatedAt(), LocalDateTime.now()).toMinutes() > BOOKING_PAYMENT_TIMEOUT_MINUTES) {
            booking.setStatus(BookingStatus.EXPIRED);
            bookingRepository.save(booking);
            throw new AppException(ErrorCode.BOOKING_EXPIRED);
        }
    }

    private void savePendingPayment(Booking booking, String txnRef) {
        Payment payment = paymentRepository.findByBooking(booking)
                .orElse(Payment.builder().booking(booking).build());
        payment.setMethod(PaymentMethod.MOMO);
        payment.setAmount(booking.getTotalPrice());
        payment.setTxnRef(txnRef);
        payment.setStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);
    }
}