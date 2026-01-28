package org.rookies.zdme.controller.api;

import lombok.RequiredArgsConstructor;
import org.rookies.zdme.dto.RefreshTokenRequest;
import org.rookies.zdme.dto.RefreshTokenResponse;
import org.rookies.zdme.service.RefreshTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class RefreshTokenController {

    private final RefreshTokenService refreshTokenService;

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            String newAccessToken = refreshTokenService.refreshAccessToken(request.getRefreshToken());
            return ResponseEntity.ok(new RefreshTokenResponse(newAccessToken));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
