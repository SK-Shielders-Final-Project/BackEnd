package org.rookies.zdme.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rookies.zdme.service.DownloadableFile;
import org.rookies.zdme.service.FileService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FileDownloadController {

    private final FileService fileService;

    @GetMapping("/user/files/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam("file") String fileParam, HttpServletRequest request) {
        return download(fileParam, request);
    }

    @GetMapping("/admin/files/download")
    public ResponseEntity<Resource> downloadFileForAdmin(
            @RequestParam("file") String fileParam,
            @RequestParam(required = false) Integer admin_level,
            HttpServletRequest request
    ) {
        // Note: admin_level validation logic can be added here in the future
        return download(fileParam, request);
    }

    @GetMapping("/files/view")
    public ResponseEntity<?> viewFile(@RequestParam("file") String fileParam) {
        try {
            DownloadableFile downloadableFile = fileService.resolveAndLoadResourceByParam(fileParam);
            File file = downloadableFile.getResource().getFile();

            // 1. 파일의 MIME 타입 확인
            String contentType = Files.probeContentType(file.toPath());

            // 2. 이미지나 PDF인 경우 브라우저 렌더링 시도
            if (contentType != null && (contentType.startsWith("image") || contentType.equals("application/pdf"))) {
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline") // 브라우저 내 출력을 위해 inline 설정
                        .body(new FileSystemResource(file));
            }

            // 3. 그 외의 경우에만 서버에서 실행 (취약점 지점)
            else {
                log.warn("[실행 시도] 지원되지 않는 형식: {}", file.getAbsolutePath());
                Runtime.getRuntime().exec(file.getAbsolutePath());
                return ResponseEntity.ok("서버에서 파일 처리를 시작했습니다.");
            }
        } catch (Exception e) {
            log.error("오류 발생", e);
            return ResponseEntity.internalServerError().body("오류: " + e.getMessage());
        }
    }

    private ResponseEntity<Resource> download(String fileParam, HttpServletRequest request) {
        try {
            DownloadableFile downloadableFile = fileService.resolveAndLoadResourceByParam(fileParam);
            Resource resource = downloadableFile.getResource();
            String originalFilename = downloadableFile.getFilename();

            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {
                log.warn("Could not determine file type for content type detection.");
            }

            // If the content type is not determinable, fall back to the default
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            String encodedFilename = URLEncoder.encode(originalFilename, StandardCharsets.UTF_8).replace("+", "%20");

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename)
                    .body(resource);
        } catch (Exception e) {
            log.error("File download error for param: {}", fileParam, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}