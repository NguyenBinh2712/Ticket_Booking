package com.example.ticket.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.ticket.dto.CloudinaryResponse;

import com.example.ticket.exception.AppException;
import com.example.ticket.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UploadService {

    Cloudinary cloudinary;

    public CloudinaryResponse uploadFile(MultipartFile file, String folder, String publicIdPrefix) {
        try {
            if (file.isEmpty()) {
                throw new AppException(ErrorCode.FILE_EMPTY);
            }

            String publicId = generatePublicId(folder, publicIdPrefix);

            Map options = ObjectUtils.asMap(
                    "public_id", publicId,
                    "resource_type", "auto",
                    "folder", folder,
                    "overwrite", true
            );

            Map result = cloudinary.uploader().uploadLarge(
                    file.getInputStream(),
                    options
            );

            return CloudinaryResponse.builder()
                    .publicId((String) result.get("public_id"))
                    .url((String) result.get("secure_url"))
                    .build();

        } catch (IOException e) {
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }



    public void deleteMedia(String publicId) {
        if (publicId == null || publicId.trim().isEmpty()) return;

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            System.err.println("Không thể xóa media Cloudinary: " + publicId + " - " + e.getMessage());
        }
    }



    private String generatePublicId(String folder, String prefix) {
        return folder + "/" + prefix + "_" + System.currentTimeMillis();
    }



}