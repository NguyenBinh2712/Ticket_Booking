package com.example.ticket.service;

import com.example.ticket.dto.CloudinaryResponse;
import com.example.ticket.dto.auth.ChangePasswordRequest;
import com.example.ticket.dto.user.UserProfileRequest;
import com.example.ticket.entity.User;
import com.example.ticket.exception.AppException;
import com.example.ticket.exception.ErrorCode;
import com.example.ticket.repository.RefreshTokenRepository;
import com.example.ticket.repository.UserRepository;
import com.example.ticket.util.FileUploadUtil;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@Transactional
public class UserService {
    UserRepository userRepository;
    UploadService uploadService;
    PasswordEncoder passwordEncoder;
    RefreshTokenRepository refreshTokenRepository;

    public void createOrUpdateProfile(UserProfileRequest request){
        User user=getCurrentUser();
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setBirth(request.getBirth());
        userRepository.save(user);
    }

    public void changeAvatar(MultipartFile file){
        User user=getCurrentUser();
        FileUploadUtil.assertAllowed(file);
        String fileName=FileUploadUtil.getFileName(file.getOriginalFilename());
        CloudinaryResponse response=uploadService.uploadFile(file, "avatar",fileName);



        if (user.getAvatarPublicId() != null) {
            uploadService.deleteMedia(user.getAvatarPublicId());
        }

        user.setAvatarUrl(response.getUrl());
        user.setAvatarPublicId(response.getPublicId());
        userRepository.save(user);
    }

    private User getCurrentUser(){
        String email= SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    public void changeMyPassword(ChangePasswordRequest request) {
        User user = getCurrentUser();

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INCORRECT_PASSWORD);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        refreshTokenRepository.deleteByUser(user);
        userRepository.save(user);
    }

}
