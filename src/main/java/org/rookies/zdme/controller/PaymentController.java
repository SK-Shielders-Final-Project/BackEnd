package org.rookies.zdme.controller;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.rookies.zdme.model.dto.PaymentSuccessDto;
import org.rookies.zdme.model.entity.Payment;
import org.rookies.zdme.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/user/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@RequestBody PaymentSuccessDto dto) {
        try {
            Payment payment = paymentService.tossPaymentConfirm(dto);

            return ResponseEntity.ok()
                    .body(Map.of(
                            "message", "결제가 정상적으로 승인되었습니다.",
                            "orderId", payment.getOrderId(),
                            "status", payment.getPaymentStatus()
                    ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
