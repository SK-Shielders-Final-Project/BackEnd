package org.rookies.zdme.dto.file;

import lombok.Builder;
import lombok.Getter;
import org.rookies.zdme.model.entity.File;

@Getter
@Builder
public class FileDetailResponseDto {
    private final Long fileId;
    private final String originalFilename;
    private final String fileDownloadUri;
    private final String fileViewUri;
    private final String ext;

    public static FileDetailResponseDto from(File file) {
        if (file == null) {
            return null;
        }
        String downloadUri = "/api/user/files/download?file=" + file.getFileId();
        String viewUri = "/api/files/view?file=" + file.getFileId();

        return FileDetailResponseDto.builder()
                .fileId(file.getFileId())
                .originalFilename(file.getOriginalName())
                .fileDownloadUri(downloadUri)
                .fileViewUri(viewUri)
                .ext(file.getExt())
                .build();
    }
}
