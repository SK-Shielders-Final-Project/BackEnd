package org.rookies.zdme.dto.file;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import org.rookies.zdme.model.entity.File;

import java.time.LocalDateTime;

@Getter
@Builder
public class FileResponseDto {
    private final Long fileId;
    private final String category;
    private final String originalName;
    private final String fileName;
    private final String ext;
    private final String path;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime createdAt;

    public static FileResponseDto from(File file) {
        if (file == null) {
            return null;
        }

        return FileResponseDto.builder()
                .fileId(file.getFileId())
                .category(file.getCategory())
                .originalName(file.getOriginalName())
                .fileName(file.getFileName())
                .ext(file.getExt())
                .path(file.getPath())
                .createdAt(file.getCreatedAt())
                .build();
    }
}
