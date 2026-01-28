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
    public ResponseEntity<FileUploadResponse> upload(
            @RequestPart("file") MultipartFile file
    ) {
        File saved = fileService.save(FIXED_CATEGORY, file);

        String uuid = saved.getFileName();
        String ext = saved.getExt();
        String storedName = uuid + "." + ext;

        return ResponseEntity.ok(
                FileUploadResponse.builder()
                        .file_id(saved.getFileId())
                        .original_name(saved.getOriginalName())
                        .path(saved.getPath())
                        .uuid(uuid)
                        .ext(ext)
                        .stored_name(storedName)
                        .build()
        );
    }

    // 사용자 다운로드: 본인 문의에 연결된 파일만 허용
    @GetMapping("/user/files/{fileId}/download")
    public ResponseEntity<Resource> downloadForUser(
            @PathVariable Long fileId
    ) {
        return buildDownloadResponse(fileId);
    }

    // 관리자 다운로드: 임시 검증 (X-ADMIN-ID == 1만 허용)
    @GetMapping("/admin/files/{fileId}/download")
    public ResponseEntity<Resource> downloadForAdmin(
            @PathVariable Long fileId
    ) {
        return buildDownloadResponse(fileId);
    }

    private ResponseEntity<Resource> buildDownloadResponse(Long fileId) {
        File meta = fileService.getMeta(fileId);
        Resource resource = fileService.loadAsResource(fileId);

        String encoded = URLEncoder.encode(meta.getOriginalName(), StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
