package org.rookies.zdme.controller.api;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.rookies.zdme.dto.*;
import org.rookies.zdme.model.entity.User;
import org.rookies.zdme.security.JwtUtil;
import org.rookies.zdme.service.SecurityService; // Import SecurityService
import org.rookies.zdme.service.UserService;
import org.springframework.http.HttpHeaders; // Import HttpHeaders
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.security.PrivateKey;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * 사용자 관련 Controller
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserAPIController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final SecurityService securityService; // Inject SecurityService
    private static final String SESSION_KEY_RSA = "RSA_PRIVATE_KEY";

    /**
     * 회원가입 (대량 할당 및 SQL 인젝션 등 취약점을 포함)
     * @param requestData
     * @return
     */
    @PostMapping("/auth/signup")
    public ResponseEntity<?> vulnerableSignup(@RequestBody Map<String, Object> requestData) {
        try {
            User newUser = userService.vulnerableSignup(requestData);
            return ResponseEntity.ok(SignupResponse.fromEntity(newUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    /**
     * 일반 사용자 jwt 로그인
     * @param authenticationRequest
     * @return
     * @throws Exception
     */
    @PostMapping("/auth/login")
    public ResponseEntity<?> createAuthenticationToken(
            @RequestBody LoginRequest authenticationRequest,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
            @RequestHeader(value = "X-Integrity-Token", required = false) String integrityToken) {
        try {
            // 1. 앱 여부 판단 (헤더가 둘 다 존재하면 앱으로 간주)
            boolean isAppRequest = (deviceId != null && !deviceId.isEmpty())
                    && (integrityToken != null && !integrityToken.isEmpty());

            // 2. 앱인 경우에만 무결성 검증 수행
            if (isAppRequest) {
                // 검증 로직 수행 (실패 시 여기서만 401 리턴)
                if (!securityService.validateIntegrityToken(integrityToken, deviceId)) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Collections.singletonMap("error", "App integrity verification failed or token expired."));
                }
            }
            // 3. 웹인 경우 (isAppRequest == false)는 위 if문을 건너뛰고 바로 로그인 진행

            // --- 기존 로그인 로직 (공통) ---
            authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
            final UserDetails userDetails = userService.loadUserByUsername(authenticationRequest.getUsername());
            final String accessToken = jwtUtil.generateToken(userDetails);
            final String refreshToken = jwtUtil.generateRefreshToken(userDetails);
            userService.saveRefreshToken(userDetails.getUsername(), refreshToken);
            final Long userId = ((User) userDetails).getUserId();

            return ResponseEntity.ok(new LoginResponse(accessToken, refreshToken, userId));
        } catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "사용자 계정이 비활성화되었습니다."));
        } catch (UsernameNotFoundException | BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "아이디 또는 비밀번호가 일치하지 않습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "로그인 처리 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/info") // URL에 ID를 노출하지 않습니다.
    public ResponseEntity<Map<String, Object>> getUserInfo(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                throw new UsernameNotFoundException("인증된 사용자 정보가 없습니다.");
            }

            // 토큰에 저장된 ID(혹은 username)를 가져옵니다.
            // 서비스 로직에 따라 username으로 조회하거나, ID로 조회하도록 변경해야 합니다.
            String username = userDetails.getUsername();

            // 예: username(ID)을 통해 DB에서 상세 정보를 조회
            Map<String, Object> userInfo = userService.getUserInfoByUsername(username);

            return new ResponseEntity<>(userInfo, HttpStatus.OK);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "회원 정보 조회 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/verify-password")
    public ResponseEntity<?> verifyPassword(Principal principal, @RequestBody VerifyPasswordRequest request) {
        try {
            boolean success = userService.verifyPassword(principal.getName(), request.getPassword());
            if (success) {
                return ResponseEntity.ok(new VerifyPasswordResponse("success"));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "비밀번호가 일치하지 않습니다."));
            }
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "비밀번호 확인 중 오류가 발생했습니다."));
        }
    }

    @PutMapping("/auth/changepw")
    public ResponseEntity<?> changePassword(Principal principal, @RequestBody ChangePasswordRequest request) {
        try {
            User updatedUser = userService.changePassword(
                    principal.getName(),
                    request.getCurrent_password(),
                    request.getNew_password()
            );
            return ResponseEntity.ok(UserUpdateResponse.fromEntity(updatedUser));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", e.getMessage()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "현재 비밀번호가 일치하지 않습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "비밀번호 변경 중 오류가 발생했습니다."));
        }
    }

    /**
     * 회원정보 수정
     * @param principal
     * @param request
     * @return
     */
    @PutMapping("/info")
    public ResponseEntity<?> updateUserInfo(
            Principal principal,
            @RequestBody UpdateUserInfoRequest request,
            @RequestHeader(value = "User-Agent", defaultValue = "") String userAgent,
            HttpSession session) {
        try {
            String rawPassword;

            // User-Agent에 "Android"가 포함되어 있는지 확인
            if (userAgent.contains("Android")) {
                System.out.println("📱 안드로이드 요청 감지: 복호화를 진행합니다.");

                PrivateKey privateKey = (PrivateKey) session.getAttribute(SESSION_KEY_RSA);
                if (privateKey == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("보안 세션 만료");
                }

                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(Cipher.DECRYPT_MODE, privateKey);

                byte[] encryptedBytes = Base64.getMimeDecoder().decode(request.getPassword());
                byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
                rawPassword = new String(decryptedBytes, StandardCharsets.UTF_8);
            } else {
                System.out.println("💻 일반(Web/기타) 요청 감지: 평문 비밀번호를 사용합니다.");
                // 안드로이드가 아니면 프론트에서 평문으로 보냈다고 가정
                rawPassword = request.getPassword();
            }
            // 3. 서비스 호출 (복호화된 비밀번호를 넘겨줌)
            User updatedUser = userService.updateUserInfo(principal.getName(), request, rawPassword);
            return ResponseEntity.ok(UserInfoPartialUpdateResponse.fromEntity(updatedUser));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", e.getMessage()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "비밀번호가 일치하지 않습니다."));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "회원 정보 수정 중 오류가 발생했습니다."));
        }
    }

    /**
     * 아이디/비밀번호 검증
     */
    private void authenticate(String username, String password) throws DisabledException, BadCredentialsException {
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }
}
