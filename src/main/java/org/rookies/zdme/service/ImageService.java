package org.rookies.zdme.service;

import org.rookies.zdme.dto.image.ImageUploadResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class ImageService {

    // ✅ 프로젝트 실행 디렉터리 기준 ./upload/
    private static final Path UPLOAD_DIR = Paths.get("upload");

    public ImageUploadResponse uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file is required");
        }

        String original = file.getOriginalFilename();
        String ext = extractExt(original);
        String filename = UUID.randomUUID() + "." + ext;

        try {
            Files.createDirectories(UPLOAD_DIR);
            Path target = UPLOAD_DIR.resolve(filename);

            // ✅ transferTo 대신 NIO로 (윈도우에서 더 안정적인 경우 많음)
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            // ✅ properties 방식에서는 file:upload/가 static root로 붙으므로 "/{filename}"로 접근됨
            return ImageUploadResponse.builder()
                    .image_url("/" + filename)
                    .build();

        } catch (IOException e) {
            // ✅ 원인 로그를 살려야 디버깅됨
            throw new RuntimeException("image upload failed: " + e.getMessage(), e);
        }
    }

    public ImageUploadResponse uploadUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("image_url is required");
        }
        // 요구사항: URL 그대로 반환
        return ImageUploadResponse.builder()
                .image_url(imageUrl)
                .build();
    }

    private String extractExt(String name) {
        if (name == null) throw new IllegalArgumentException("invalid file name");
        int idx = name.lastIndexOf(".");
        if (idx == -1 || idx == name.length() - 1) throw new IllegalArgumentException("invalid file ext");
        return name.substring(idx + 1).toLowerCase();
    }
}
