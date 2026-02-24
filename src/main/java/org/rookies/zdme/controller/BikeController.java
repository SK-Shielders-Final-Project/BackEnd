package org.rookies.zdme.controller;

import java.util.List;

import org.rookies.zdme.dto.bike.BikeListRequest;
import org.rookies.zdme.dto.bike.BikeResponse;
import org.rookies.zdme.dto.bike.BikeReturnRequestDto; // BikeReturnRequestDto 임포트 추가
import org.rookies.zdme.dto.bike.BikeReturnResponseDto; // BikeReturnResponseDto 임포트 추가
import org.rookies.zdme.service.BikeService;
import org.rookies.zdme.service.DownloadableFile;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
@RequestMapping("/api")
public class BikeController {

    private final BikeService bikeService;

    public BikeController(BikeService bikeService) {
        this.bikeService = bikeService;
    }

    @PostMapping({"/bikes"})
    public ResponseEntity<List<BikeResponse>> list() {
        return ResponseEntity.ok(bikeService.listAll());
    }


    @PutMapping("/admin/bike")
    public ResponseEntity<?> updateStatus(
            @RequestBody org.rookies.zdme.dto.bike.BikeStatusUpdateRequest request // response body
    ) {
        return ResponseEntity.ok(
                bikeService.updateStatus(request.getBike_id(), request.getStatus())
        );
    }

    @PostMapping("/bikes/return")
    public ResponseEntity<BikeReturnResponseDto> returnBike(@RequestBody BikeReturnRequestDto requestDto) {
        BikeReturnResponseDto responseDto = bikeService.returnBike(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/user/bikes/{serialNumber}")
    public ResponseEntity<Resource> getBikeReturnPhoto(@PathVariable String serialNumber, HttpServletRequest request) {
        // 1. 서비스를 통해 파일 리소스를 가져옴
        DownloadableFile downloadableFile = bikeService.getBikeReturnPhoto(serialNumber);
        Resource resource = downloadableFile.getResource();

        // 2. 파일의 Content-Type 결정
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            // Content-Type을 결정할 수 없는 경우, 기본값으로 설정
            contentType = "application/octet-stream";
        }

        // 3. ResponseEntity를 사용하여 파일 데이터와 헤더를 함께 반환
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                // Content-Disposition을 "inline"으로 설정하면 브라우저가 이미지를 바로 표시하려고 시도
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + downloadableFile.getFilename() + "\"")
                .body(resource);
    }
}
