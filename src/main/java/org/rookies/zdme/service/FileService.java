package org.rookies.zdme.service;

import java.util.Optional;
import org.rookies.zdme.model.entity.File;
import org.rookies.zdme.repository.FileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FileService {

    private final FileRepository fileRepository;

    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @Transactional
    public Optional<File> saveFromFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) return Optional.empty();

        String ext = "";
        int dot = fileName.lastIndexOf('.');
        if (dot > -1 && dot < fileName.length() - 1) ext = fileName.substring(dot + 1);

        File file = File.builder()
                .category("INQUIRY")
                .originalName(fileName)
                .fileName(fileName)
                .ext(ext)
                .path("")
                .build();

        return Optional.of(fileRepository.save(file));
    }
}
