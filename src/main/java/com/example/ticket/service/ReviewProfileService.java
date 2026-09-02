package com.example.ticket.service;

import com.example.ticket.dto.profile_business_partner.ProfileResponse;
import com.example.ticket.dto.profile_business_partner.ProfileReviewRequest;
import com.example.ticket.entity.ProducerProfile;
import com.example.ticket.entity.VenueProfile;
import com.example.ticket.enums.ProfileStatus;
import com.example.ticket.exception.AppException;
import com.example.ticket.exception.ErrorCode;
import com.example.ticket.repository.ProducerProfileRepository;
import com.example.ticket.repository.VenueProfileRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class ReviewProfileService {

    ProducerProfileRepository producerProfileRepository;
    VenueProfileRepository venueProfileRepository;

    public List<ProfileResponse> getPendingProducers() {
        return producerProfileRepository.findByStatus(ProfileStatus.PENDING_VERIFICATION)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<ProfileResponse> getPendingVenues() {
        return venueProfileRepository.findByStatus(ProfileStatus.PENDING_VERIFICATION)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public void reviewProducer(Long producerId, ProfileReviewRequest request) {
        ProducerProfile profile = producerProfileRepository.findById(producerId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCER_PROFILE_NOT_FOUND));

        validateReview(profile.getStatus(), request);
        profile.setStatus(request.getApprove() ? ProfileStatus.VERIFIED : ProfileStatus.REJECTED);
        profile.setStatus(request.getApprove() ? ProfileStatus.VERIFIED : ProfileStatus.REJECTED);
        if (!request.getApprove()) {
            profile.setRejectReason(request.getRejectReason());
        }
        producerProfileRepository.save(profile);
    }

    public void reviewVenue(Long venueId, ProfileReviewRequest request) {
        VenueProfile profile = venueProfileRepository.findById(venueId)
                .orElseThrow(() -> new AppException(ErrorCode.VENUE_PROFILE_NOT_FOUND));

        validateReview(profile.getStatus(), request);
        profile.setStatus(request.getApprove() ? ProfileStatus.VERIFIED : ProfileStatus.REJECTED);
        profile.setStatus(request.getApprove() ? ProfileStatus.VERIFIED : ProfileStatus.REJECTED);
        if (!request.getApprove()) {
            profile.setRejectReason(request.getRejectReason());
        }
        venueProfileRepository.save(profile);
    }

    private void validateReview(ProfileStatus currentStatus, ProfileReviewRequest request) {
        if (currentStatus == ProfileStatus.VERIFIED) {
            throw new AppException(ErrorCode.PROFILE_ALREADY_REVIEWED);
        }
        if (!request.getApprove() &&
                (request.getRejectReason() == null || request.getRejectReason().isBlank())) {
            throw new AppException(ErrorCode.REJECT_REASON_REQUIRED);
        }
    }

    private ProfileResponse toResponse(ProducerProfile p) {
        return ProfileResponse.builder()
                .id(p.getId()).userId(p.getUser().getId()).userEmail(p.getUser().getEmail())
                .name(p.getCompanyName()).description(p.getDescription())
                .status(p.getStatus()).createdAt(p.getCreatedAt()).build();
    }

    private ProfileResponse toResponse(VenueProfile p) {
        return ProfileResponse.builder()
                .id(p.getId()).userId(p.getUser().getId()).userEmail(p.getUser().getEmail())
                .name(p.getVenueName()).description(p.getDescription())
                .status(p.getStatus()).createdAt(p.getCreatedAt()).build();
    }
}