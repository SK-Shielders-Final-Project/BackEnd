package org.rookies.zdme.controller.api;

import jakarta.servlet.http.HttpServletRequest;
import org.rookies.zdme.dto.ForgotPasswordRequest;
import org.rookies.zdme.dto.PasswordResetRequest;
import org.rookies.zdme.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth/password-reset")
public class PasswordResetController {

    private final UserService userService;

    public PasswordResetController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/request")
    public ResponseEntity<Void> requestReset(HttpServletRequest request, @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        String host = request.getHeader("Host");
        boolean success = userService.requestPasswordReset(forgotPasswordRequest.getUsername(), forgotPasswordRequest.getEmail(), host);
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<Void> reset(@RequestBody PasswordResetRequest passwordResetRequest) {
        boolean success = userService.resetPassword(passwordResetRequest.getToken(), passwordResetRequest.getNewPassword());
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}
