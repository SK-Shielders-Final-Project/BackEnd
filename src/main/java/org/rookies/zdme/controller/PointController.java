package org.rookies.zdme.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.rookies.zdme.dto.GiftHistoryResponseDto;
import org.rookies.zdme.dto.GiftRequestDto;
import org.rookies.zdme.dto.GiftResponseDto;
import org.rookies.zdme.service.PointService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.Principal;
import java.security.PrivateKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/user/point")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;
    private static final String SESSION_KEY_AES = "AES_SYMMETRIC_KEY";

    @PostMapping("/gift")
    public ResponseEntity<?> giftPoints(@RequestBody GiftRequestDto reqDto, HttpSession session) {
        try {

            Key aesKey = (Key) session.getAttribute(SESSION_KEY_AES);
            if(aesKey == null) {
                return ResponseEntity.status(400).body(null);
            }

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, aesKey);

            byte[] encryptedBytes = Base64.getDecoder().decode(reqDto.getEncryptedPayload());
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            String decryptedJson = new String(decryptedBytes, StandardCharsets.UTF_8);

            System.out.println("🔓 [서버] 복호화된 데이터: " + decryptedJson);

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> data = mapper.readValue(decryptedBytes, Map.class);

            String receiver = (String) data.get("receiverName");
            String sender = (String) data.get("senderName");
            Number amountNum = (Number) data.get("amount");
            Long amount = amountNum.longValue();


            pointService.sendGift(sender, receiver, amount);

            System.out.println("✅ " + sender + "님이 " + receiver + "님에게 " + amount + "포인트를 선물했습니다.");

            String resultMessage = "{\"status\": \"success\", \"message\": \"포인트 선물 완료!\"}";

            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            byte[] resultByte = cipher.doFinal(resultMessage.getBytes(StandardCharsets.UTF_8));
            String encryptedResult = Base64.getEncoder().encodeToString(resultByte);

            return ResponseEntity.ok(GiftResponseDto.builder().encryptedResult(encryptedResult).build());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/gift/history")
    public ResponseEntity<?> getGiftHistory() {
        List<GiftHistoryResponseDto> resDto = pointService.getGiftHistoryList();
        return ResponseEntity.ok(resDto);
    }

}
