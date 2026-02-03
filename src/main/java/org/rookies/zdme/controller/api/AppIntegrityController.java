package org.rookies.zdme.controller.api;

import lombok.RequiredArgsConstructor;
import org.rookies.zdme.dto.app.AppIntegrityRequest;
import org.rookies.zdme.dto.app.AppIntegrityResponse;
import org.rookies.zdme.service.AppIntegrityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app")
@RequiredArgsConstructor
public class AppIntegrityController {

    private final AppIntegrityService appIntegrityService;

    @PostMapping("/verify-integrity")
    public ResponseEntity<AppIntegrityResponse> verifyIntegrity(@RequestBody AppIntegrityRequest request) {
        boolean isValid = appIntegrityService.verify(request);
        return ResponseEntity.ok(new AppIntegrityResponse(isValid));
    }
}
