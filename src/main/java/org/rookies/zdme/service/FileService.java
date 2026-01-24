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
    public Resource loadAsResource(Long fileId) {
        File file = getMeta(fileId);

        Path baseDir = Paths.get(props.getBaseDir()).toAbsolutePath().normalize();
        Path filePath = baseDir
                .resolve(file.getPath())
                .resolve(file.getFileName() + "." + file.getExt())
                .normalize();

        if (!filePath.startsWith(baseDir)) {
            throw new BadRequestException("invalid file path");
        }

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new NotFoundException("file not found on disk");
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new BadRequestException("invalid file url");
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
