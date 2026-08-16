package com.example.ticket.service;

import com.example.ticket.dto.profile_business_partner.ProducerProfileRequest;
import com.example.ticket.dto.profile_business_partner.ProfileResponse;
import com.example.ticket.dto.profile_business_partner.ProfileReviewRequest;
import com.example.ticket.entity.ProducerProfile;
import com.example.ticket.entity.User;
import com.example.ticket.enums.ProfileStatus;
import com.example.ticket.exception.AppException;
import com.example.ticket.exception.ErrorCode;
import com.example.ticket.repository.ProducerProfileRepository;
import com.example.ticket.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@Transactional
public class ProducerProfileService {
    ProducerProfileRepository producerProfileRepository;
    UserRepository userRepository;

    private User getCurrentUser(){
        String email= SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findUserByEmail(email)
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    public void createProfile(ProducerProfileRequest request){
        User user=getCurrentUser();
        if(producerProfileRepository.existsByUser(user)){
            throw new AppException(ErrorCode.PRODUCER_PROFILE_EXISTED);
        }
        ProducerProfile profile = ProducerProfile.builder()
                .user(user)
                .companyName(request.getCompanyName())
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .description(request.getDescription())
                .status(ProfileStatus.PENDING_VERIFICATION)
                .build();
        producerProfileRepository.save(profile);
    }

    public ProfileResponse getMyProfile(){
        User user=getCurrentUser();
        ProducerProfile profile = producerProfileRepository.findByUser(user)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCER_PROFILE_NOT_FOUND));
        return toResponse(profile);
    }

    public ProfileResponse getProductProfile(Long userId){
        User user=userRepository.findById(userId)
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_EXISTED));
        ProducerProfile profile=producerProfileRepository.findByUser(user)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCER_PROFILE_NOT_FOUND));
        return toResponse(profile);
    }

    public void updateProfile(ProducerProfileRequest request) {
        User user = getCurrentUser();
        ProducerProfile profile = producerProfileRepository.findByUser(user)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCER_PROFILE_NOT_FOUND));

        profile.setCompanyName(request.getCompanyName());
        profile.setContactEmail(request.getContactEmail());
        profile.setContactPhone(request.getContactPhone());
        profile.setDescription(request.getDescription());
        if (profile.getStatus() == ProfileStatus.REJECTED) {
            profile.setStatus(ProfileStatus.PENDING_VERIFICATION);
        }

        producerProfileRepository.save(profile);
    }

    private ProfileResponse toResponse(ProducerProfile profile){
        return ProfileResponse.builder()
                .id(profile.getId())
                .userId(profile.getUser().getId())
                .userEmail(profile.getUser().getEmail())
                .name(profile.getCompanyName())
                .description(profile.getDescription())
                .status(profile.getStatus())
                .createdAt(profile.getCreatedAt())
                .build();
    }
}
