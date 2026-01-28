package org.rookies.zdme.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.rookies.zdme.service.DownloadableFile;
import org.rookies.zdme.service.FileService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
        // TODO: admin_level에 따른 권한 검증 로직 추가 가능
        return download(fileParam, request);
    }

    private ResponseEntity<Resource> download(String fileParam, HttpServletRequest request) {
        DownloadableFile downloadableFile = fileService.resolveAndLoadResourceByParam(fileParam);
        Resource resource = downloadableFile.getResource();
        String originalFilename = downloadableFile.getFilename();

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            // contentType을 결정할 수 없는 경우 기본값으로 설정
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        String encodedFilename = URLEncoder.encode(originalFilename, StandardCharsets.UTF_8).replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename)
                .body(resource);
    }
}
