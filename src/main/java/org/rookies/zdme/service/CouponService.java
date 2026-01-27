package org.rookies.zdme.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.rookies.zdme.dto.CouponRequestDto;
import org.rookies.zdme.dto.CouponResponseDto;
import org.rookies.zdme.model.entity.UsedCoupon;
import org.rookies.zdme.model.entity.User;
import org.rookies.zdme.repository.UsedCouponRepository;
import org.rookies.zdme.repository.UserRepository;
import org.rookies.zdme.security.SecurityUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final UsedCouponRepository usedCouponRepository;
    private final UserRepository userRepository;

    private static final String SECRET_KEY = "jacdangmobil";

    @Transactional
    public CouponResponseDto redeemCoupon(CouponRequestDto dto) {
        String username = SecurityUtil.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        String couponCode = dto.getCouponCode();
        String decryptedPlainText = decryptCoupon(couponCode);

        if (decryptedPlainText.length() != 13) {
            throw new IllegalArgumentException("유효하기 않은 쿠폰 형식입니다.");
        }

        String dateStr = decryptedPlainText.substring(0, 8);
        Long amount = Long.parseLong(decryptedPlainText.substring(8));

        try {
            UsedCoupon usedCoupon = UsedCoupon.builder()
                    .userId(user.getUserId())
                    .couponCode(couponCode)
                    .build();

            usedCouponRepository.save(usedCoupon);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("이미 사용된 쿠폰입니다.");
        }

        user.updatePoint(amount);
        return CouponResponseDto.builder()
                .userId(user.getUserId())
                .totalPoint(user.getTotalPoint())
                .rechargedPoint(amount)
                .build();
    }

    private String decryptCoupon(String couponCode) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(couponCode);
            String decodeXor = new String(decodedBytes);

            StringBuilder plainText = new StringBuilder();
            for (int i = 0; i<decodeXor.length(); i++) {
                plainText.append((char) (decodeXor.charAt(i) ^ SECRET_KEY.charAt(i % SECRET_KEY.length())));
            }
            return plainText.toString();
        } catch (Exception e) {
            throw new IllegalArgumentException("잘못된 쿠폰 코드입니다. ");
        }
    }
}
