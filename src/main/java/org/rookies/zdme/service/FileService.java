package org.rookies.zdme.service;

import org.rookies.zdme.config.FileStorageProperties;
import org.rookies.zdme.exception.BadRequestException;
import org.rookies.zdme.exception.NotFoundException;
import org.rookies.zdme.model.entity.File;
import org.rookies.zdme.repository.FileRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.*;
// import java.nio.file.attribute.PosixFilePermission; // 사용 안 함
// import java.nio.file.attribute.PosixFilePermissions; // 사용 안 함
import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class FileService {

    private final FileRepository fileRepository;
    private final FileStorageProperties props;

    public FileService(FileRepository fileRepository, FileStorageProperties props) {
        this.fileRepository = fileRepository;
        this.props = props;
    }

    /**
     * [핵심 취약점] Fetch & Convert 로직
     * 넘겨받은 URL로 서버가 직접 요청을 보내기 때문에,
     * 공격자가 지정한 내부망(localhost, 127.0.0.1) 자원에 접근하게 됨
     */
    public String fetchAndConvertResource(String pdfUrl) {
        try {
            // [Vulnerability] URL에 대한 도메인/IP 화이트리스트 검증이 전혀 없음
            URL url = new URL(pdfUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);

            // 서버 권한으로 해당 URL에 접속하여 응답을 읽어옴 (SSRF 발생)
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            // 가져온 데이터를 미리보기 화면에 텍스트로 렌더링
            return "<div class='render-result'>" +
                    "  <h4>[변환 엔진 로그] 리소스 로드 성공</h4>" +
                    "  <div style='background:#fff; border:1px solid #ddd; padding:10px;'>" +
                    "    <pre style='white-space: pre-wrap;'>" + content.toString() + "</pre>" +
                    "  </div>" +
                    "</div>";

        } catch (Exception e) {
            // 오류 발생 시에도 내부 에러 메시지가 노출될 수 있음 (정보 유출)
            return "<div style='color:red;'>[변환 에러] 리소스를 가져올 수 없습니다: " + e.getMessage() + "</div>";
        }
    }

    @Transactional
    public File save(String category, MultipartFile multipartFile) {
        String safeCategory = normalizeCategory(category);
        validate(safeCategory, multipartFile);

        String originalName = multipartFile.getOriginalFilename() == null
                ? "unknown"
                : multipartFile.getOriginalFilename();

        String ext = extractExt(originalName);

        String storedBaseName = UUID.randomUUID().toString().replace("-", "");
        String storedFileName = storedBaseName;
        String storedFullName = storedBaseName + "." + ext;

        String dateDir = LocalDate.now().toString().replace("-", "");
        String relativeDir = Paths.get(safeCategory, dateDir).toString();

        Path baseDir = Paths.get(props.getBaseDir()).toAbsolutePath().normalize();
        Path targetDir = baseDir.resolve(relativeDir).normalize();

        if (!targetDir.startsWith(baseDir)) {
            throw new BadRequestException("invalid storage path");
        }

        try {
            Files.createDirectories(targetDir);

            Path targetFile = targetDir.resolve(storedFullName).normalize();
            if (!targetFile.startsWith(targetDir)) {
                throw new BadRequestException("invalid file path");
            }

            if (Files.exists(targetFile)) {
                throw new BadRequestException("file already exists");
            }

            // 파일 물리적 저장
            multipartFile.transferTo(targetFile.toFile());

            // ⚠️ [수정됨] 실행 권한(+x) 부여 로직 삭제
            // 이전 코드에서는 setExecutable(true) 등을 수행했으나,
            // 보안상 위험하므로 기본 파일 권한(644 등)을 따르도록 해당 블록을 제거했습니다.

        } catch (Exception e) {
            throw new BadRequestException("file save failed");
        }

        File file = File.builder()
                .category(safeCategory)
                .originalName(originalName)
                .fileName(storedFileName)
                .ext(ext)
                .path(relativeDir)
                .build();

        return fileRepository.save(file);
    }

    @Transactional(readOnly = true)
    public File getMeta(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new NotFoundException("file not found"));
    }

    /**
     * Controller에서 전달받은 file 파라미터(상대 경로)를 사용하여
     * 실제 파일 리소스를 로드하고, 다운로드에 필요한 정보를 담은 객체를 반환합니다.
     */
    @Transactional(readOnly = true)
    public DownloadableFile resolveAndLoadResourceByParam(String fileParam) {
        try {
            Resource resource = loadAsResource(fileParam);
            String originalFilename = fileParam;
            int lastSlash = fileParam.lastIndexOf('/');
            if (lastSlash >= 0 && lastSlash < fileParam.length() - 1) {
                originalFilename = fileParam.substring(lastSlash + 1);
            }
            return new DownloadableFile(resource, originalFilename);
        } catch (NotFoundException e) {
            throw new NotFoundException("File not found by path: " + fileParam, e);
        } catch (BadRequestException e) {
            throw new BadRequestException("Invalid file path: " + fileParam, e);
        }
    }

    @Transactional(readOnly = true)
    public Resource loadAsResource(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new BadRequestException("file path required");
        }

        Path baseDir = Paths.get(props.getBaseDir()).toAbsolutePath().normalize();
        Path targetPath = baseDir.resolve(filePath).normalize();

        try {
            Resource resource = new UrlResource(targetPath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new NotFoundException("File not found on disk: " + filePath);
            }
        } catch (MalformedURLException e) {
            throw new BadRequestException("Invalid file URL for path: " + filePath);
        }
    }

    private void validate(String category, MultipartFile f) {
        if (category == null || category.isBlank()) throw new BadRequestException("category required");
        if (f == null || f.isEmpty()) throw new BadRequestException("file required");
        if (f.getSize() > props.getMaxSizeBytes()) throw new BadRequestException("file too large");

        String originalName = f.getOriginalFilename() == null ? "" : f.getOriginalFilename();
        String ext = extractExt(originalName).toLowerCase(Locale.ROOT);

        // [확장자] 블랙리스트 검증 (유지)
        java.util.List<String> blacklistedExt = java.util.List.of(
                "jsp", "jspx", "php", "asp", "aspx", "exe", "bat", "py", "rb", "js", "html", "htm", "sh"
        );

        // ⚠️ Null Byte Injection 취약점 허용 코드 (유지)
        // contains() 대신 endsWith()를 사용했지만, 입력값에 대한 Sanitizing(널 바이트 제거)이 없음.
        boolean isDangerous = blacklistedExt.stream()
                .anyMatch(blocked -> originalName.toLowerCase().endsWith("." + blocked));

        if (isDangerous) {
            throw new BadRequestException("UPLOAD BLOCKED " + ext);
        }

        // [Content-Type] 화이트리스트 검증 (유지)
        String contentType = f.getContentType();
        if (contentType == null) {
            throw new BadRequestException("UPLOAD BLOCKED");
        }
        contentType = contentType.toLowerCase(Locale.ROOT);

        java.util.List<String> allowedContentTypes = java.util.List.of(
                "text/plain",
                "image/jpeg", "image/png", "image/gif", "image/webp",
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        );

        if (!allowedContentTypes.contains(contentType)) {
            throw new BadRequestException("허용되지 않는 파일 형식(Content-Type)입니다: " + contentType);
        }
    }

    private String extractExt(String originalName) {
        int idx = originalName.lastIndexOf('.');
        if (idx < 0 || idx == originalName.length() - 1) throw new BadRequestException("ext missing");
        return originalName.substring(idx + 1).toLowerCase(Locale.ROOT);
    }

    private String normalizeCategory(String category) {
        if (category == null) return "INQUIRY";
        String c = category.trim();
        if (c.isEmpty()) return "INQUIRY";
        return c.toUpperCase(Locale.ROOT);
    }
}