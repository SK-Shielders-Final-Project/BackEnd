package org.rookies.zdme.controller;

import org.rookies.zdme.model.entity.File;
import org.rookies.zdme.service.FileService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class FileController {

    private static final String FIXED_CATEGORY = "INQUIRY";
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    // 기존 업로드 로직
    @PostMapping(value = "/files/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<java.util.Map<String, Object>> upload(
            @RequestPart("file") MultipartFile file
    ) {
        try {
            File saved = fileService.save(FIXED_CATEGORY, file);
            return ResponseEntity.ok(java.util.Map.of("result", "Y", "file_id", saved.getFileId()));
        } catch (org.rookies.zdme.exception.BadRequestException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("result", "N"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(java.util.Map.of("result", "N"));
        }
    }

    /**
     * [취약점 지점] PDF URL을 받아 미리보기용 텍스트/이미지 HTML로 변환
     * 사용자가 입력한 URL을 검증 없이 서비스 레이어로 전달함
     */
//    @GetMapping(value = "/files/preview-render", produces = MediaType.TEXT_HTML_VALUE)
//    public ResponseEntity<String> previewPdfFromUrl( @RequestParam("pdfUrl") String pdfUrl) {
//
//        // WAS가 렌더링 엔진(Service)에게 원본 주소를 그대로 전달
//        String renderedContent = fileService.fetchAndConvertResource(pdfUrl);
//
//        String htmlTemplate = "<html>" +
//                "<head><title>Remote Document Preview</title></head>" +
//                "<body style='font-family: sans-serif; padding: 20px;'>" +
//                "  <h2>문서 미리보기 결과</h2>" +
//                "  <p style='color: gray;'>Source: " + pdfUrl + "</p>" +
//                "  <hr />" +
//                "  <div class='preview-window' style='border: 1px solid #ccc; padding: 15px; background: #f9f9f9;'>" +
//                     renderedContent + // 렌더러가 가져온 결과물 주입
//                "  </div>" +
//                "</body>" +
//                "</html>";
//
//        return ResponseEntity.ok().body(htmlTemplate);
//    }
}