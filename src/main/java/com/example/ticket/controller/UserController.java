package com.example.ticket.controller;

import com.example.ticket.dto.ApiResponse;
import com.example.ticket.dto.auth.ChangePasswordRequest;
import com.example.ticket.dto.user.UserProfileRequest;
import com.example.ticket.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("isAuthenticated()")
public class UserController {

    UserService userService;

    @PutMapping("/me/profile")
    public ApiResponse<String> updateMyProfile(
            @RequestBody @Valid UserProfileRequest request) {

        userService.createOrUpdateProfile(request);

        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Profile updated successfully");
        return response;
    }

    @PutMapping(
            value = "/me/avatar",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<String> changeMyAvatar(
            @RequestParam("file") MultipartFile file) {

        userService.changeAvatar(file);

        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Avatar updated successfully");
        return response;
    }

    @PutMapping("/me/password")
    public ApiResponse<String> changeMyPassword(
            @RequestBody @Valid ChangePasswordRequest request) {

        userService.changeMyPassword(request);

        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Password changed successfully. Please login again.");
        return response;
    }
}