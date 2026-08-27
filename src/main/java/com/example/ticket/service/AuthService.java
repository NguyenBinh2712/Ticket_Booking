package com.example.ticket.service;

import com.example.ticket.constant.RedisKey;
import com.example.ticket.dto.auth.*;
import com.example.ticket.dto.otp.OtpRequest;
import com.example.ticket.dto.otp.ResendOtpRequest;
import com.example.ticket.entity.Otp;
import com.example.ticket.entity.RefreshToken;
import com.example.ticket.entity.User;
import com.example.ticket.enums.OtpType;
import com.example.ticket.enums.Role;
import com.example.ticket.enums.UserStatus;
import com.example.ticket.exception.AppException;
import com.example.ticket.exception.ErrorCode;
import com.example.ticket.repository.OtpRepository;
import com.example.ticket.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@Transactional
public class AuthService {
    private static long OTP_EXPIRE_SECONDS = 60;
    private static long OTP_WINDOW_HOURS = 1;
    private static int MAX_OTP_PER_HOUR = 5;

    private static SecureRandom RANDOM = new SecureRandom();
    PasswordEncoder passwordEncoder;
    UserRepository userRepository;
    JwtService jwtService;
    RefreshTokenService refreshTokenService;
    MailService mailService;
    OtpRepository otpRepository;
    RedisTemplate<String, Object> redisTemplate;

    public AuthResponse login(AuthRequest request){
        User user=userRepository.findUserByEmail(request.getEmail())
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_EXISTED));
        boolean authenticated=passwordEncoder.matches(request.getPassword(), user.getPassword());
        if(!authenticated){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AppException(ErrorCode.USER_LOCKED);
        }

        JwtRequest jwt=JwtRequest.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
        String accessToken= jwtService.generateAccessToken(jwt);
        String refreshToken= refreshTokenService.createRefreshToken(user);
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }


    public AuthResponse refresh(RefreshRequest request){
        RefreshToken oldToken = refreshTokenService.verify(request.getToken());
        User user = oldToken.getUser();
        refreshTokenService.revoked(oldToken.getRawToken());
        JwtRequest jwtRequest=JwtRequest.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
        String newRefreshToken = refreshTokenService.createRefreshToken(user);

        String accessToken = jwtService.generateAccessToken(jwtRequest);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    public void logout(LogoutRequest request) {
        refreshTokenService.revoked(request.getToken());
    }

    public void register(RegisterRequest request){
        if (request.getRole() == Role.ADMIN) {
            throw new AppException(ErrorCode.INVALID_ROLE_FOR_ACTION);
        }
        if(userRepository.existsByEmail(request.getEmail())){
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        User user=User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .createdAt(LocalDateTime.now())
                .status(UserStatus.LOCKED)
                .build();
        userRepository.save(user);
        String otpCode =generateOtp();
        checkOtpLimit(request.getEmail());
        Otp otp=Otp.builder()
                .otp(otpCode)
                .createAt(Instant.now())
                .exp(Instant.now().plus(60, ChronoUnit.SECONDS))
                .type(OtpType.REGISTER)
                .user(user)
                .build();
        otpRepository.save(otp);


        mailService.sendEmail(
                user.getEmail(),
                "Xác thực tài khoản",
                "Mã xác thực của bạn là: " + otpCode
        );

    }

    private String generateOtp() {
        return String.valueOf(
                100000 + RANDOM.nextInt(900000)
        );
    }
    private void checkOtpLimit(String email) {
        String cooldownKey = RedisKey.OTP_COOLDOWN + email;
        String countKey = RedisKey.OTP_COUNT + email;
        // Atomic: chỉ set được nếu key CHƯA tồn tại (SETNX) -
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(cooldownKey, 1, Duration.ofSeconds(OTP_EXPIRE_SECONDS));
        if (Boolean.FALSE.equals(acquired)) {
            throw new AppException(ErrorCode.OTP_SEND_TOO_FAST);
        }
        Long current = redisTemplate.opsForValue().increment(countKey);
        if (current != null && current == 1) {
            redisTemplate.expire(countKey, Duration.ofHours(OTP_WINDOW_HOURS));
        }
        if (current != null && current > MAX_OTP_PER_HOUR) {
            redisTemplate.opsForValue().decrement(countKey);
            throw new AppException(ErrorCode.OTP_LIMIT_EXCEEDED);
        }
    }

    public void verifyOtp(OtpRequest request) {
        User user = userRepository.findUserByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Otp otp = otpRepository.findByUserAndOtpAndType(user, request.getOtp(), OtpType.REGISTER)
                .orElseThrow(()->new AppException(ErrorCode.INVALID_OTP));


        if (otp.getExp().isBefore(Instant.now())) {
            otpRepository.delete(otp);
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }

        user.setStatus(UserStatus.ACTIVE);
        otpRepository.delete(otp);
        userRepository.save(user);
    }

    public void resendOtp(ResendOtpRequest request) {

        User user = userRepository.findUserByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        checkOtpLimit(user.getEmail());
        otpRepository.deleteByUserAndType(user, OtpType.REGISTER);
        String otpCode = generateOtp();
        Otp otp = Otp.builder()
                .otp(otpCode)
                .createAt(Instant.now())
                .exp(Instant.now().plus(60, ChronoUnit.SECONDS))
                .user(user)
                .type(OtpType.REGISTER)
                .build();
        otpRepository.save(otp);
        mailService.sendEmail(
                user.getEmail(),
                "Xác thực tài khoản",
                "Mã xác thực của bạn là: " + otpCode + "\nHiệu lực trong 60 giây."
        );
    }

    public void requestForgotPasswordOtp(String email) {
        userRepository.findUserByEmail(email).ifPresent(user -> {
            checkOtpLimit(user.getEmail());
            otpRepository.deleteByUserAndType(user, OtpType.FORGOT_PASSWORD);
            String otpCode = generateOtp();
            Otp otp = Otp.builder()
                    .otp(otpCode)
                    .createAt(Instant.now())
                    .exp(Instant.now().plus(60, ChronoUnit.SECONDS))
                    .user(user)
                    .type(OtpType.FORGOT_PASSWORD)
                    .build();
            otpRepository.save(otp);

            mailService.sendEmail(
                    user.getEmail(),
                    "Yêu cầu đặt lại mật khẩu",
                    "Mã xác thực để đặt lại mật khẩu: " + otpCode + "\nHiệu lực trong 60 giây."
            );
        });
    }
    public void verifyOtpForgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findUserByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Otp otp = otpRepository.findByUserAndOtpAndType(user, request.getOtp(), OtpType.FORGOT_PASSWORD)
                .orElseThrow(()->new AppException(ErrorCode.INVALID_OTP));

        if (otp.getExp().isBefore(Instant.now())) {
            otpRepository.delete(otp);
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        otpRepository.delete(otp);
        userRepository.save(user);

        mailService.sendEmail(
                request.getEmail(),
                "Thông báo thay đổi mật khẩu",
                "Mật khẩu đã được cập nhật thành công. Hãy đăng nhập lại."
        );
    }

}
