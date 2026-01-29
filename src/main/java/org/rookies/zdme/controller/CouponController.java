package org.rookies.zdme.controller;

import lombok.RequiredArgsConstructor;
import org.rookies.zdme.dto.CouponRequestDto;
import org.rookies.zdme.dto.CouponResponseDto;
import org.rookies.zdme.security.SecurityUtil;
import org.rookies.zdme.service.CouponService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/coupon")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/redeem")
    public ResponseEntity<?> redeemCouponCode(@RequestBody CouponRequestDto reqDto) {
        try {
            CouponResponseDto resDto = couponService.redeemCoupon(reqDto);
            return ResponseEntity.ok(resDto);
        }
        catch (IllegalArgumentException e) {
            HttpStatus status = "이미 사용된 쿠폰입니다.".equals(e.getMessage())
                    ? HttpStatus.CONFLICT
                    : HttpStatus.BAD_REQUEST;

            Map<String, String> errorResponse = Collections.singletonMap("message", e.getMessage());

            return ResponseEntity.status(status).body(errorResponse);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Collections.singletonMap("message", "서버 내부 오류가 발생했습니다."));
        }
    }
}
