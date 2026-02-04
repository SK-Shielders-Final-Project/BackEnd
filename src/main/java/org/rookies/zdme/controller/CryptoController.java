package org.rookies.zdme.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.connector.Response;
import org.rookies.zdme.dto.GiftRequestDto;
import org.rookies.zdme.dto.GiftResponseDto;
import org.rookies.zdme.dto.KeyExchangeRequestDto;
import org.rookies.zdme.service.PointService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/crypto")
public class CryptoController {

    private static final String SESSION_KEY_RSA = "RSA_PRIVATE_KEY";
    private static final String SESSION_KEY_AES = "AES_SYMMETRIC_KEY";

//    @GetMapping("/public-key")
//    public ResponseEntity<?> generateKeyPair(HttpSession session) {
//        try {
//            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
//            keyPairGenerator.initialize(2048);
//            KeyPair keyPair = keyPairGenerator.generateKeyPair();
//
//            PublicKey publicKey = keyPair.getPublic();
//            PrivateKey privateKey = keyPair.getPrivate();
//
//            session.setAttribute(SESSION_KEY_RSA, privateKey);
//
//            String encodedPublicKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
//
//            Map<String, String> response = new HashMap<>();
//            response.put("publicKey", encodedPublicKey);
//
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.internalServerError().build();
//        }
//    }

    @GetMapping("/public-key")
    public ResponseEntity<?> generateKeyPair(HttpServletRequest request) {
        try {
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
                System.out.println("기존 세션 삭제 완료 (초기화)");
            }

            HttpSession newSession = request.getSession(true);

            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            // 4. [저장 단계] 아까 만든 '새로운 세션'에 저장
            newSession.setAttribute(SESSION_KEY_RSA, privateKey);

            String encodedPublicKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());

            Map<String, String> response = new HashMap<>();
            response.put("publicKey", encodedPublicKey);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/exchange-key")
    public ResponseEntity<?> exchangeKey(@RequestBody KeyExchangeRequestDto reqDto, HttpSession session) {
        try {
            PrivateKey privateKey = (PrivateKey) session.getAttribute(SESSION_KEY_RSA);
            if (privateKey == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("키 교환 세션이 만료되었습니다. 페이지를 새로고침 해주세요.");
            }

            // RSA 복호화 설정
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            byte[] encryptedBytes = Base64.getDecoder().decode(reqDto.getEncryptedSymmetricKey());
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            // 복호화된 바이트로 AES 키 생성
            Key aesKey = new SecretKeySpec(decryptedBytes, "AES");

            session.setAttribute(SESSION_KEY_AES, aesKey);

            return ResponseEntity.ok("대칭키 교환 및 저장 완료");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("대칭키 교환 처리 중 오류 발생");
        }
    }

//    private static final Map<String, PrivateKey> privateKeyStore = new HashMap<>();
//    private final KeyStore keyStore;
//
//    // public key 생성
//    // 취약 버전 : userId에 비밀 키 값 저장
//    @GetMapping("/public-key")
//    public ResponseEntity<?> generateKeyPair(Principal principal) {
//        if (principal == null) {
//            return ResponseEntity.status(401).build();
//        }
//        try {
//            String userId = principal.getName();
//
//            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
//            keyPairGenerator.initialize(2048);
//            KeyPair keyPair = keyPairGenerator.generateKeyPair();
//
//            PublicKey publicKey = keyPair.getPublic();
//            PrivateKey privateKey = keyPair.getPrivate();
//
//            privateKeyStore.put(userId, privateKey);
//
//            String encodedPublicKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
//
//            Map<String, String> response = new HashMap<>();
//            response.put("publicKey", encodedPublicKey);
//
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError().build();
//        }
//    }
//
//    @PostMapping("/exchange-key")
//    public ResponseEntity<?> exchangeKey(@RequestBody KeyExchangeRequestDto reqDto, Principal principal) {
//        if (principal == null) {
//            return ResponseEntity.status(401).body("로그인이 필요합니다.");
//        }
//        try {
//            String userId = principal.getName();
//
//            PrivateKey privateKey = privateKeyStore.get(userId);
//
//            if (privateKey == null) {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("키 교환 세션이 만료되었습니다.");
//            }
//
//            Cipher cipher = Cipher.getInstance("RSA");
//            cipher.init(Cipher.DECRYPT_MODE, privateKey);
//
//            byte[] encryptedBytes = Base64.getDecoder().decode(reqDto.getEncryptedSymmetricKey());
//            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
//
//            Key aesKey = new SecretKeySpec(decryptedBytes, "AES");
//
//            keyStore.putKey(userId, aesKey);
//
//            return ResponseEntity.ok("대칭키 교환 및 저장 완료");
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("대칭키 교환 처리 중 오류 발생");
//        }
//    }
}
