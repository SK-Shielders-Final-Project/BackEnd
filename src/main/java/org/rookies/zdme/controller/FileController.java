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


}
