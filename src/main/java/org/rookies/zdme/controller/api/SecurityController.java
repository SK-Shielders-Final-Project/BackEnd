package org.rookies.zdme.controller.api;

import org.rookies.zdme.dto.security.NonceResponseDto;
import org.rookies.zdme.dto.security.VerifyRequestDto;
import org.rookies.zdme.dto.security.VerifyResponseDto;
import org.rookies.zdme.service.SecurityService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/security")
public class SecurityController {

    private final SecurityService securityService;

    public SecurityController(SecurityService securityService) {
        this.securityService = securityService;
    }

    @GetMapping("/challenge")
    public ResponseEntity<NonceResponseDto> getNonce() {
        String nonce = securityService.generateNonce();
        return ResponseEntity.ok(new NonceResponseDto(nonce));
    }

    @PostMapping("/verify")
    public ResponseEntity<VerifyResponseDto> verifyIntegrity(
            @RequestBody VerifyRequestDto requestDto,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId) { // assuming X-Device-Id is sent in header
        System.out.println("Verify Step - deviceId: " + deviceId);
        String integrityToken = securityService.verifyIntegrityAndIssueToken(
                requestDto.getNonce(),
                requestDto.getBinaryHash(),
                requestDto.getSignatureHash(),
                deviceId
        );
        return ResponseEntity.ok(new VerifyResponseDto(integrityToken));
    }
}
