package org.rookies.zdme.controller;

import org.rookies.zdme.dto.image.ImageUploadResponse;
import org.rookies.zdme.dto.image.ImageUploadUrlRequest;
import org.rookies.zdme.service.ImageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponse> uploadFile(
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.ok(imageService.uploadFile(file));
    }

    @PostMapping(value = "/image", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ImageUploadResponse> uploadUrl(
            @RequestBody ImageUploadUrlRequest body
    ) {
        return ResponseEntity.ok(imageService.uploadUrl(body.getImage_url()));
    }
}
