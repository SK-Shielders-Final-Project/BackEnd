package org.rookies.zdme.controller;

import org.rookies.zdme.dto.file.FileUploadResponse;
import org.rookies.zdme.exception.ForbiddenException;
import org.rookies.zdme.model.entity.File;
import org.rookies.zdme.repository.InquiryRepository;
import org.rookies.zdme.service.FileService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api")
public class FileController {

    private static final String FIXED_CATEGORY = "INQUIRY";

    private final FileService fileService;
    private final InquiryRepository inquiryRepository;

    public FileController(FileService fileService, InquiryRepository inquiryRepository) {
        this.fileService = fileService;
        this.inquiryRepository = inquiryRepository;
    }

    // 파일 업로드 (사용자) - category 고정
    @PostMapping(value = "/files/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<java.util.Map<String, String>> upload(
            @RequestPart("file") MultipartFile file
    ) {
        try {
            fileService.save(FIXED_CATEGORY, file);
            return ResponseEntity.ok(java.util.Map.of("result", "Y"));
        } catch (org.rookies.zdme.exception.BadRequestException e) {
            // Bad request exceptions from FileService (validation, etc.)
            return ResponseEntity.badRequest().body(java.util.Map.of("result", "N"));
        } catch (Exception e) {
            // Other unexpected exceptions
            return ResponseEntity.internalServerError().body(java.util.Map.of("result", "N"));
        }
    }


}
