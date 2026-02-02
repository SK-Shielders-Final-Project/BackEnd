package org.rookies.zdme.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.rookies.zdme.dto.GiftRequestDto;
import org.rookies.zdme.dto.GiftResponseDto;
import org.rookies.zdme.dto.KeyExchangeRequestDto;
import org.rookies.zdme.security.KeyStore;
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

    private static final Map<String, PrivateKey> privateKeyStore = new HashMap<>();
    private final KeyStore keyStore;

    // public key 생성
    // 취약 버전 : userId에 비밀 키 값 저장
    @GetMapping("/public-key")
    public ResponseEntity<?> generateKeyPair(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            String userId = principal.getName();

            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            privateKeyStore.put(userId, privateKey);

            String encodedPublicKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());

            Map<String, String> response = new HashMap<>();
            response.put("publicKey", encodedPublicKey);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/exchange-key")
    public ResponseEntity<?> exchangeKey(@RequestBody KeyExchangeRequestDto reqDto, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }
        try {
            String userId = principal.getName();

            PrivateKey privateKey = privateKeyStore.get(userId);

            if (privateKey == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("키 교환 세션이 만료되었습니다.");
            }

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            byte[] encryptedBytes = Base64.getDecoder().decode(reqDto.getEncryptedSymmetricKey());
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            Key aesKey = new SecretKeySpec(decryptedBytes, "AES");

            keyStore.putKey(userId, aesKey);

            return ResponseEntity.ok("대칭키 교환 및 저장 완료");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("대칭키 교환 처리 중 오류 발생");
        }
    }

    // 안전한 버전 : 기기 마다 (핸드세이크 기준)
//    @GetMapping("/public-key")
//    public ResponseEntity<Map<String, String>> generateKeyPair(@RequestParam String handshakeId) {
//        if (handshakeId == null || handshakeId.isEmpty()) {
//            return ResponseEntity.badRequest().build();
//        }
//
//        try {
//            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
//            keyGen.initialize(2048);
//            KeyPair keyPair = keyGen.generateKeyPair();
//
//            // handshakeId로 하여 동시 접속 문제 해결
//            privateKeyStore.put(handshakeId, keyPair.getPrivate());
//
//            String encodedPublicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
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
//    /**
//     * 2단계: 키 교환 (DTO에 deviceId 포함 필요)
//     */
//    @PostMapping("/exchange-key")
//    public ResponseEntity<String> exchangeKey(@RequestBody KeyExchangeRequestDto request) {
//        try {
//            String deviceId = request.getHandshakeId(); // DTO에 필드 추가 필요
//
//            PrivateKey privateKey = privateKeyStore.get(deviceId);
//            if (privateKey == null) {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("키가 만료되었습니다. 다시 시도해주세요.");
//            }
//
//            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//            cipher.init(Cipher.DECRYPT_MODE, privateKey);
//
//            byte[] encryptedBytes = Base64.getDecoder().decode(request.getEncryptedSymmetricKey());
//            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
//
//            Key aesKey = new SecretKeySpec(decryptedBytes, "AES");
//
//            // 최종 대칭키 저장 (이제 deviceId로 암호화 통신을 하게 됨)
//            symmetricKeyStore.put(deviceId, aesKey);
//
//            // 사용한 비대칭키 삭제
//            privateKeyStore.remove(deviceId);
//
//            return ResponseEntity.ok("성공");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.internalServerError().body("오류");
//        }
//    }


}
