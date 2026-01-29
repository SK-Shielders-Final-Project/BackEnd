package org.rookies.zdme.controller;

import org.rookies.zdme.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
public class JwkSetController {

    private final JwtUtil jwtUtil;

    @Autowired
    public JwkSetController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * 공개키
     * @return
     */
    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<Map<String, Object>> getJwkSet() {
        Map<String, Object> jwk = jwtUtil.getJwk();
        Map<String, Object> jwkSet = Collections.singletonMap("keys", Collections.singletonList(jwk));
        return ResponseEntity.ok(jwkSet);
    }
}
