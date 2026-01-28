package org.rookies.zdme.dto.file;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileUploadResponse {
    private Long file_id;
    private String original_name;

    // ✅ 추가 반환값
    private String path;        // ex) INQUIRY/20260124
    private String uuid;        // ex) 3f9c...
    private String ext;         // ex) pdf
    private String stored_name; // ex) 3f9c....pdf
}
