package org.rookies.zdme.dto.image;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ImageUploadResponse {
    private String image_url;
}
