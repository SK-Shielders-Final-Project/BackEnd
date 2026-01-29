package org.rookies.zdme.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rookies.zdme.service.DownloadableFile;
import org.rookies.zdme.service.FileService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FileDownloadController {

    private final FileService fileService;

    /**
     * 문의사항 첨부파일 미리보기 API
     * 이미지 파일은 브라우저에 렌더링하고,
     * 그 외의 문서 파일은 서버 내 뷰어를 통해 스냅샷 생성을 시도함.
     */
    @GetMapping("/files/view")
    public ResponseEntity<?> viewFile(@RequestParam("file") String fileParam) {
        try {
            DownloadableFile downloadableFile = fileService.resolveAndLoadResourceByParam(fileParam);
            File file = downloadableFile.getResource().getFile();
            String fileName = file.getName().toLowerCase();

            // 1. 이미지 파일(.jpg, .png)은 정상적으로 브라우저에 보여줌
            if (fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".jpeg")) {
                return serveImage(file);
            }

            // 2. [취약점 발생 지점] 이미지 외의 파일(문서 등)은 서버 OS의 기본 프로그램을 실행하여
            //    미리보기 스냅샷을 생성하도록 설계됨 (실제로는 RCE 취약점)
            else {
                log.warn("[미리보기 생성 전처리] 문서 파일 실행 시도: {}", file.getAbsolutePath());

                // 개발자의 변명: "오픈오피스나 PDF 뷰어가 서버에서 이 파일을 열어 스냅샷을 찍을 거야!"
                // 현실: .exe나 .sh 파일이 들어오면 그대로 서버에서 실행됨
                Runtime.getRuntime().exec(file.getAbsolutePath());

                return ResponseEntity.ok()
                        .body("문서 미리보기를 생성 중입니다. 시스템 사양에 따라 5~10초가 소요될 수 있습니다.");
            }

        } catch (IOException e) {
            log.error("파일 접근 오류", e);
            return ResponseEntity.internalServerError().body("파일을 찾을 수 없습니다.");
        } catch (Exception e) {
            log.error("미리보기 처리 중 오류", e);
            return ResponseEntity.internalServerError().body("미리보기 엔진 오류 발생");
        }
    }

    /**
     * 이미지를 ResponseEntity로 변환하여 브라우저에 전송
     */
    private ResponseEntity<Resource> serveImage(File file) throws IOException {
        Resource resource = new FileSystemResource(file);
        String contentType = Files.probeContentType(file.toPath());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType != null ? contentType : "image/jpeg"))
                .body(resource);
    }
}