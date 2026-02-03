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
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
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

            // ✅ 추가된 로직: 리눅스 환경에서 실행 권한(+x) 부여
            try {
                // 권한 설정: rwxr-xr-x (모든 사용자 읽기 및 실행 가능)
                Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxr-xr-x");
                Files.setPosixFilePermissions(targetFile, perms);
            } catch (UnsupportedOperationException e) {
                // 윈도우 등 POSIX를 지원하지 않는 OS일 경우의 Fallback
                targetFile.toFile().setExecutable(true, false);
            }

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
     *
     * @param fileParam Controller가 URL의 'file' 쿼리 파라미터로부터 받은 값
     * @return 다운로드할 파일의 Resource와 원본 파일명을 포함하는 DownloadableFile 객체
     */
    @Transactional(readOnly = true)
    public DownloadableFile resolveAndLoadResourceByParam(String fileParam) {
        try {
            // 1. fileParam을 사용하여 실제 파일 시스템으로부터 파일을 로드하고 Resource 객체로 변환합니다.
            Resource resource = loadAsResource(fileParam);

            // 2. fileParam 경로에서 마지막 '/' 이후의 문자열을 추출하여 원본 파일명으로 사용합니다.
            //    (실제 파일 시스템에 저장된 이름이 아닌, 다운로드 시 사용자에게 보여줄 이름)
            String originalFilename = fileParam;
            int lastSlash = fileParam.lastIndexOf('/');
            if (lastSlash >= 0 && lastSlash < fileParam.length() - 1) {
                originalFilename = fileParam.substring(lastSlash + 1);
            }

            // 3. Resource와 파일명을 담은 DTO(DownloadableFile)를 생성하여 반환합니다.
            return new DownloadableFile(resource, originalFilename);
        } catch (NotFoundException e) {
            throw new NotFoundException("File not found by path: " + fileParam, e);
        } catch (BadRequestException e) {
            throw new BadRequestException("Invalid file path: " + fileParam, e);
        }
    }

    /**
     * 주어진 파일 경로(상대 경로)를 기반으로 파일 시스템에서 실제 파일을 찾아
     * Spring의 Resource 객체로 로드합니다.
     *
     * @param filePath 다운로드할 파일의 상대 경로 (e.g., "INQUIRY/20240131/some-file.txt")
     * @return 파일 시스템의 파일을 가리키는 UrlResource 객체
     */
    @Transactional(readOnly = true)
    public Resource loadAsResource(String filePath) {
        // 1. 파일 경로가 유효한지 확인합니다.
        if (filePath == null || filePath.isBlank()) {
            throw new BadRequestException("file path required");
        }

        // 2. 설정 파일(application.properties)에 정의된 기본 저장 위치(app.file.base-dir=upload)를 가져옵니다.
        Path baseDir = Paths.get(props.getBaseDir()).toAbsolutePath().normalize();
        // 3. 기본 경로와 전달받은 상대 경로를 결합하여 파일의 전체 절대 경로를 생성합니다.
        //    예: C:\\project\\upload + INQUIRY\20240131\file.txt -> C:\\project\\upload\INQUIRY\20240131\file.txt
        Path targetPath = baseDir.resolve(filePath).normalize();

        try {
            // 4. 완성된 파일 경로(URI)를 기반으로 UrlResource 객체를 생성합니다.
            Resource resource = new UrlResource(targetPath.toUri());
            // 5. 해당 경로에 파일이 실제로 존재하고 읽기 가능한지 확인한 후, 리소스를 반환합니다.
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                // 파일이 없거나 읽을 수 없으면 예외를 발생시킵니다.
                throw new NotFoundException("File not found on disk: " + filePath);
            }
        } catch (MalformedURLException e) {
            // 파일 경로가 유효하지 않은 URL 형식일 경우 예외를 발생시킵니다.
            throw new BadRequestException("Invalid file URL for path: " + filePath);
        }
    }

    private void validate(String category, MultipartFile f) {
        if (category == null || category.isBlank()) throw new BadRequestException("category required");
        if (f == null || f.isEmpty()) throw new BadRequestException("file required");
        if (f.getSize() > props.getMaxSizeBytes()) throw new BadRequestException("file too large");

        String originalName = f.getOriginalFilename() == null ? "" : f.getOriginalFilename();
        String ext = extractExt(originalName).toLowerCase(Locale.ROOT);

        // [확장자] 블랙리스트 검증
        java.util.List<String> blacklistedExt = java.util.List.of(
                "jsp", "jspx", "php", "asp", "aspx", "exe", "bat", "py", "rb", "js", "html", "htm"
        );

        if (blacklistedExt.contains(ext)) {
            throw new BadRequestException("UPLOAD BLOCKED" + ext);
        }

        // [Content-Type] 화이트리스트 검증
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

        boolean isAllowedType = allowedContentTypes.contains(contentType);

        if (!isAllowedType) {
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