package org.rookies.zdme.controller.api;

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
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
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
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "회원가입 처리 중 심각한 오류가 발생했습니다."));
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
            userService.checkUserRole(authenticationRequest.getUsername());
            final UserDetails userDetails = userService.loadUserByUsername(authenticationRequest.getUsername());
            final String token = jwtUtil.generateToken(userDetails);
            return ResponseEntity.ok(new LoginResponse(token));
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
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/verify-password")
    public ResponseEntity<?> verifyPassword(Principal principal, @RequestBody VerifyPasswordRequest request) {
        boolean success = userService.verifyPassword(principal.getName(), request.getPassword());
        if (success) {
            return ResponseEntity.ok(new VerifyPasswordResponse("success"));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PutMapping("/auth/changepw")
    public ResponseEntity<UserUpdateResponse> changePassword(Principal principal, @RequestBody ChangePasswordRequest request) {
        User updatedUser = userService.changePassword(
                principal.getName(),
                request.getCurrent_password(),
                request.getNew_password()
        );
        return ResponseEntity.ok(UserUpdateResponse.fromEntity(updatedUser));
    }

    /**
     * 회원정보 수정
     * @param principal
     * @param request
     * @return
     */
    @PutMapping("/info")
    public ResponseEntity<UserInfoPartialUpdateResponse> updateUserInfo(Principal principal, @RequestBody UpdateUserInfoRequest request) {
        User updatedUser = userService.updateUserInfo(principal.getName(), request);
        return ResponseEntity.ok(UserInfoPartialUpdateResponse.fromEntity(updatedUser));
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
