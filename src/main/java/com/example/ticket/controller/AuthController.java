package com.example.ticket.controller;

import com.example.ticket.dto.ApiResponse;
import com.example.ticket.dto.auth.*;
import com.example.ticket.dto.otp.OtpRequest;
import com.example.ticket.dto.otp.ResendOtpRequest;
import com.example.ticket.service.AuthService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequestMapping("/auth")
public class AuthController {
    AuthService authService;

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(
            @RequestBody @Valid AuthRequest request) {

        ApiResponse apiResponse=new ApiResponse();
        apiResponse.setResult(authService.login(request));
        return apiResponse;
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout(
            @RequestBody LogoutRequest request) {

        authService.logout(request);
        ApiResponse apiResponse=new ApiResponse();
        apiResponse.setMessage("logout success");
        return apiResponse;
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refreshToken(
            @RequestBody RefreshRequest request) throws Exception {

        ApiResponse apiResponse=new ApiResponse();
        apiResponse.setResult(authService.refresh(request));
        return apiResponse;
    }

    @PostMapping("/register")
    public ApiResponse registerUser(@RequestBody @Valid RegisterRequest request) {
        ApiResponse apiResponse = new ApiResponse();
        authService.register(request);
        apiResponse.setResult("register success");
        return apiResponse;
    }

    @PostMapping("/register/verify")
    public ApiResponse verifyOtp(@RequestBody OtpRequest request) {
        authService.verifyOtp(request);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Xác thực OTP thành công");
        return apiResponse;
    }

    @PostMapping("/register/resend-otp")
    public ApiResponse resendOtp(@RequestBody ResendOtpRequest request) {
        authService.resendOtp(request);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Gửi lại OTP thành công");
        return apiResponse;
    }
    @PostMapping("/forgot-password/request-otp")
    public ApiResponse requestForgotPasswordOtp(@RequestParam String email) {
        authService.requestForgotPasswordOtp(email);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("OTP đã được gửi đến email");
        return apiResponse;
    }

    @PutMapping("/forgot-password")
    public ApiResponse forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        authService.verifyOtpForgotPassword(request);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Đặt lại mật khẩu thành công");
        return apiResponse;
    }
}
