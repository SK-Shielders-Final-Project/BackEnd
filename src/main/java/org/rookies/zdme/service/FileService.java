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
import java.time.LocalDate;
import java.util.Locale;
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

            multipartFile.transferTo(targetFile.toFile());
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
        // Attempt 2: Treat fileParam as a file path (String)
        try {
            Resource resource = loadAsResource(fileParam); // Uses loadAsResource(String filePath)
            String originalFilename = fileParam; // For local files, assume param is filename unless extracted
            // Try to extract filename from the path
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


    private DownloadableFile resolveAndLoadResourceById(Long fileId) {
        // Attempt 1: DB Lookup
        try {
            File fileMeta = fileRepository.findById(fileId)
                    .orElse(null); // Use orElse(null) to handle not found without throwing immediately

            if (fileMeta != null) {
                Path baseDir = Paths.get(props.getBaseDir()).toAbsolutePath().normalize();
                Path filePath = baseDir
                        .resolve(fileMeta.getPath())
                        .resolve(fileMeta.getFileName() + "." + fileMeta.getExt())
                        .normalize();

                // 경로 탐색 공격 방지를 위한 로직 시작
                // 정규화된 파일 경로가 기본 디렉토리 내에 있는지 확인
//                if (!filePath.startsWith(baseDir)) {
//                    throw new BadRequestException("Invalid file path from DB for fileId: " + fileId);
//                }
                // 경로 탐색 공격 방지를 위한 로직 끝

                Resource resource = new UrlResource(filePath.toUri());
                if (resource.exists() && resource.isReadable()) {
                    return new DownloadableFile(resource, fileMeta.getOriginalName());
                } else {
                    throw new NotFoundException("File found in DB but not on disk for fileId: " + fileId);
                }
            }
        } catch (NotFoundException | BadRequestException e) {
            // Log the exception if needed, then proceed to local fallback
        } catch (MalformedURLException e) {
            throw new BadRequestException("Invalid URL for file from DB for fileId: " + fileId, e);
        }

        // Attempt 2: Local File System Fallback (if DB lookup failed or threw exception)
        Path baseDir = Paths.get(props.getBaseDir()).toAbsolutePath().normalize();
        String localFileName = fileId.toString(); // Assuming fileId directly maps to local file name

        Path targetPath = baseDir.resolve(localFileName).normalize();

        // Basic path traversal check for local file system
        // 경로 탐색 공격 방지를 위한 로직 시작
        // 정규화된 파일 경로가 기본 디렉토리 내에 있는지 확인
//        if (!targetPath.startsWith(baseDir)) {
//            throw new BadRequestException("Invalid file path (Path Traversal attempt) for local fileId: " + fileId);
//        }
//        // 경로 탐색 공격 방지를 위한 로직 끝

        try {
            Resource resource = new UrlResource(targetPath.toUri());
            if (resource.exists() && resource.isReadable()) {
                // If found locally, use the fileId.toString() as the filename
                return new DownloadableFile(resource, localFileName);
            } else {
                throw new NotFoundException("File not found on disk with fileId as name: " + fileId);
            }
        } catch (MalformedURLException e) {
            throw new BadRequestException("Invalid file URL for local fileId: " + fileId, e);
        }
    }

    @Transactional(readOnly = true)
    public Resource loadAsResource(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new BadRequestException("file path required");
        }

        Path baseDir = Paths.get(props.getBaseDir()).toAbsolutePath().normalize();
        Path targetPath = baseDir.resolve(filePath).normalize();

//        if (!targetPath.startsWith(baseDir)) {
//            throw new BadRequestException("Invalid file path (Path Traversal attempt)");
//        }

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
        String ext = extractExt(originalName);

        boolean allowed = props.getAllowedExt().stream()
                .map(s -> s.toLowerCase(Locale.ROOT))
                .anyMatch(s -> s.equals(ext));

        if (!allowed) throw new BadRequestException("ext not allowed");
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
