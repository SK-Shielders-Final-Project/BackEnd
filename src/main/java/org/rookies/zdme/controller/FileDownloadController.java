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

//    @GetMapping("/files/view")
//    public ResponseEntity<?> viewFile(@RequestParam("file") String fileParam) {
//        try {
//            DownloadableFile downloadableFile = fileService.resolveAndLoadResourceByParam(fileParam);
//            File file = downloadableFile.getResource().getFile();
//
//            // 1. 파일의 MIME 타입 확인
//            String contentType = Files.probeContentType(file.toPath());
//
//            // 2. 이미지나 PDF인 경우 브라우저 렌더링 시도
//            if (contentType != null && (contentType.startsWith("image") || contentType.equals("application/pdf"))) {
//                return ResponseEntity.ok()
//                        .contentType(MediaType.parseMediaType(contentType))
//                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline") // 브라우저 내 출력을 위해 inline 설정
//                        .body(new FileSystemResource(file));
//            }
//
//            // 3. 그 외의 경우에만 서버에서 실행 (취약점 지점)
//            else {
//                log.warn("[실행 시도] 지원되지 않는 형식: {}", file.getAbsolutePath());
//                Runtime.getRuntime().exec(file.getAbsolutePath());
//                return ResponseEntity.ok("서버에서 파일 처리를 시작했습니다.");
//            }
//        } catch (Exception e) {
//            log.error("오류 발생", e);
//            return ResponseEntity.internalServerError().body("오류: " + e.getMessage());
//        }
//    }

    private ResponseEntity<Resource> download(String fileParam, HttpServletRequest request) {
        try {
            // 1. fileParam(URL의 file 쿼리 파라미터)을 기반으로 실제 파일 리소스를 찾고, 다운로드 가능한 파일 정보를 가져옵니다.
            // 이 과정은 FileService에서 처리됩니다.
            DownloadableFile downloadableFile = fileService.resolveAndLoadResourceByParam(fileParam);
            Resource resource = downloadableFile.getResource(); // 실제 파일 데이터에 접근하기 위한 Resource 객체
            String originalFilename = downloadableFile.getFilename(); // 원본 파일명

            // 2. 파일의 Content-Type(MIME 타입)을 결정합니다.
            // 예: "image/jpeg", "application/pdf"
            String contentType = null;
            try {
                // 서블릿 컨텍스트를 통해 파일 경로로부터 MIME 타입을 유추합니다.
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {
                log.warn("Could not determine file type for content type detection.");
            }

            // 3. Content-Type을 결정할 수 없는 경우, 기본적인 바이너리 파일 타입으로 설정합니다.
            // "application/octet-stream"은 종류를 알 수 없는 이진 데이터를 의미합니다.
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            // 4. 다운로드될 파일의 이름을 URL 인코딩합니다.
            // 한글 등 비 ASCII 문자가 포함된 파일명이 깨지지 않도록 하기 위함입니다.
            String encodedFilename = URLEncoder.encode(originalFilename, StandardCharsets.UTF_8).replace("+", "%20");

            // 5. 최종적으로 HTTP 응답(ResponseEntity)을 생성하여 반환합니다.
            return ResponseEntity.ok()
                    // (a) Content-Type 헤더 설정 (브라우저에게 파일 종류를 알려줌)
                    .contentType(MediaType.parseMediaType(contentType))
                    // (b) Content-Disposition 헤더 설정
                    // "attachment"는 파일을 브라우저에서 열지 않고 다운로드하도록 지시합니다.
                    // "filename*=" 부분은 RFC 5987 표준에 따라 인코딩된 파일 이름을 전달합니다.
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename)
                    // (c) 응답 본문(body)에 실제 파일 리소스(데이터)를 담아 전송합니다.
                    .body(resource);
        } catch (Exception e) {
            // 다운로드 과정에서 오류 발생 시 로그를 남기고, 서버 내부 오류(500) 응답을 반환합니다.
            log.error("File download error for param: {}", fileParam, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}