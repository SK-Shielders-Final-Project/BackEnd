package org.rookies.zdme.controller.api;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.rookies.zdme.dto.*;
import org.rookies.zdme.model.entity.User;
import org.rookies.zdme.security.JwtUtil;
import org.rookies.zdme.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginRequest authenticationRequest) {
        try {
            authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
            final UserDetails userDetails = userService.loadUserByUsername(authenticationRequest.getUsername());
            final String accessToken = jwtUtil.generateToken(userDetails);
            final String refreshToken = jwtUtil.generateRefreshToken(userDetails);
            userService.saveRefreshToken(userDetails.getUsername(), refreshToken);
            final Long userId = ((User) userDetails).getUserId();
            return ResponseEntity.ok(new LoginResponse(accessToken, refreshToken, userId));
        } catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "사용자 계정이 비활성화되었습니다."));
        } catch (UsernameNotFoundException e) { // For security, treat UsernameNotFound as invalid credentials
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "아이디 또는 비밀번호가 일치하지 않습니다."));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "아이디 또는 비밀번호가 일치하지 않습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "로그인 처리 중 오류가 발생했습니다."));
        }
    }

    /**
     * 회원정보 조회
     * @param userId
     * @return
     */
    @GetMapping("/info/{user_id}")
    public ResponseEntity<Map<String, Object>> getUserInfo(@PathVariable("user_id") Long userId) {
        try {
            Map<String, Object> userInfo = userService.getUserInfo(userId);
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
    public ResponseEntity<?> updateUserInfo(Principal principal, @RequestBody UpdateUserInfoRequest request, HttpSession session) {
        try {
            // 1. 세션에서 개인키 가져오기
            PrivateKey privateKey = (PrivateKey) session.getAttribute(SESSION_KEY_RSA);
            if (privateKey == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("보안 세션이 만료되었습니다. 새로고침 해주세요.");
            }

            // 2. 비밀번호 복호화
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            byte[] encryptedBytes = Base64.getDecoder().decode(request.getPassword());
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            // 진짜 비밀번호 추출
            String rawPassword = new String(decryptedBytes, StandardCharsets.UTF_8);

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
