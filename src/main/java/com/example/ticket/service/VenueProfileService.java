package com.example.ticket.service;

import com.example.ticket.dto.profile_business_partner.ProfileResponse;
import com.example.ticket.dto.profile_business_partner.VenueProfileRequest;
import com.example.ticket.entity.ProducerProfile;
import com.example.ticket.entity.User;
import com.example.ticket.entity.VenueProfile;
import com.example.ticket.enums.ProfileStatus;
import com.example.ticket.exception.AppException;
import com.example.ticket.exception.ErrorCode;
import com.example.ticket.repository.UserRepository;
import com.example.ticket.repository.VenueProfileRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class VenueProfileService {

    VenueProfileRepository venueProfileRepository;
    UserRepository userRepository;

    private User getCurrentUser(){
        String email= SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findUserByEmail(email)
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    public void createProfile(VenueProfileRequest request) {
        User user = getCurrentUser();

        if (venueProfileRepository.existsByUser(user)) {
            throw new AppException(ErrorCode.VENUE_PROFILE_EXISTED);
        }

        VenueProfile profile = VenueProfile.builder()
                .user(user)
                .venueName(request.getVenueName())
                .address(request.getAddress())
                .city(request.getCity())
                .areaSqm(request.getAreaSqm())
                .description(request.getDescription())
                .status(ProfileStatus.PENDING_VERIFICATION)
                .build();

        venueProfileRepository.save(profile);
    }

    public ProfileResponse getMyProfile() {
        User user = getCurrentUser();
        VenueProfile profile = venueProfileRepository.findByUser(user)
                .orElseThrow(() -> new AppException(ErrorCode.VENUE_PROFILE_NOT_FOUND));
        return toResponse(profile);
    }

    public ProfileResponse getVenueProfile(Long userId){
        User user=userRepository.findById(userId)
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_EXISTED));
        VenueProfile profile=venueProfileRepository.findByUser(user)
                .orElseThrow(() -> new AppException(ErrorCode.VENUE_PROFILE_NOT_FOUND));
        return toResponse(profile);
    }

    public void updateProfile(VenueProfileRequest request) {
        User user = getCurrentUser();
        VenueProfile profile = venueProfileRepository.findByUser(user)
                .orElseThrow(() -> new AppException(ErrorCode.VENUE_PROFILE_NOT_FOUND));

        profile.setVenueName(request.getVenueName());
        profile.setAddress(request.getAddress());
        profile.setCity(request.getCity());
        profile.setAreaSqm(request.getAreaSqm());
        profile.setDescription(request.getDescription());
        if (profile.getStatus() == ProfileStatus.REJECTED) {
            profile.setStatus(ProfileStatus.PENDING_VERIFICATION);
        }

        venueProfileRepository.save(profile);
    }

    private ProfileResponse toResponse(VenueProfile p) {
        return ProfileResponse.builder()
                .id(p.getId())
                .userId(p.getUser().getId())
                .userEmail(p.getUser().getEmail())
                .name(p.getVenueName())
                .description(p.getDescription())
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .build();
    }
}