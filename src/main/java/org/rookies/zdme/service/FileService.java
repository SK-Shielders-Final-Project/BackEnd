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

@Service
public class FileService {

    private final FileRepository fileRepository;
    private final FileStorageProperties props;

    public FileService(FileRepository fileRepository, FileStorageProperties props) {
        this.fileRepository = fileRepository;
        this.props = props;
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