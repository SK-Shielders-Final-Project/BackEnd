package org.rookies.zdme.service;

import lombok.RequiredArgsConstructor;
import org.rookies.zdme.model.entity.User;
import org.rookies.zdme.repository.UserRepository;
import org.rookies.zdme.security.JwtUtil;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public String refreshAccessToken(String refreshToken) {
        String username = jwtUtil.getUsernameFromToken(refreshToken);
        UserDetails userDetails = userService.loadUserByUsername(username);
        User user = (User) userDetails;

        if (!user.getRefreshToken().equals(refreshToken) || !jwtUtil.validateToken(refreshToken, userDetails)) {
            throw new RuntimeException("Invalid refresh token");
        }

        return jwtUtil.generateToken(userDetails);
    }
}
