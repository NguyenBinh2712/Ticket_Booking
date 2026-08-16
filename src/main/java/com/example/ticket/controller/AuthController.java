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
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {

    AuthService authService;

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(
            @RequestBody @Valid AuthRequest request) {

        ApiResponse<AuthResponse> response = new ApiResponse<>();
        response.setResult(authService.login(request));
        response.setMessage("Login successfully");

        return response;
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout(
            @RequestBody @Valid LogoutRequest request) {

        authService.logout(request);

        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Logout successfully");

        return response;
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refreshToken(
            @RequestBody @Valid RefreshRequest request) {

        ApiResponse<AuthResponse> response = new ApiResponse<>();
        response.setResult(authService.refresh(request));

        return response;
    }

    @PostMapping("/register")
    public ApiResponse<String> register(
            @RequestBody @Valid RegisterRequest request) {

        authService.register(request);

        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Register successfully");
        return response;
    }

    @PostMapping("/register/verify")
    public ApiResponse<String> verifyRegisterOtp(
            @RequestBody @Valid OtpRequest request) {
        authService.verifyOtp(request);
        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("OTP verified successfully");
        return response;
    }

    @PostMapping("/register/resend-otp")
    public ApiResponse<String> resendRegisterOtp(
            @RequestBody @Valid ResendOtpRequest request) {
        authService.resendOtp(request);
        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("OTP sent successfully");
        return response;
    }

    @PostMapping("/forgot-password/request-otp")
    public ApiResponse<String> requestForgotPasswordOtp(
            @RequestParam String email) {
        authService.requestForgotPasswordOtp(email);
        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("OTP sent successfully");
        return response;
    }

    @PutMapping("/forgot-password")
    public ApiResponse<String> forgotPassword(
            @RequestBody @Valid ForgotPasswordRequest request) {
        authService.verifyOtpForgotPassword(request);
        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Password reset successfully");
        return response;
    }
}