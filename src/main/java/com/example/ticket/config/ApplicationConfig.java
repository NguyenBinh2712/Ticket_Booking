package com.example.ticket.config;

import com.example.ticket.entity.User;
import com.example.ticket.enums.AuthProvider;
import com.example.ticket.enums.Role;
import com.example.ticket.enums.UserStatus;
import com.example.ticket.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ApplicationConfig {

    PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository) {
        return args -> {
            String adminEmail = "admin@gmail.com";

            if (userRepository.findUserByEmail(adminEmail).isEmpty()) {
                User admin = User.builder()
                        .fullName("Administrator")
                        .email(adminEmail)
                        .password(passwordEncoder.encode("admin123"))
                        .phone("0123456789")
                        .role(Role.ADMIN)
                        .status(UserStatus.ACTIVE)
                        .authProvider(AuthProvider.LOCAL)
                        .build();

                userRepository.save(admin);
                log.info("Admin user created successfully: {} / admin123", adminEmail);
            } else {
                log.info(" Admin user already exists.");
            }
        };
    }
}