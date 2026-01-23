package org.rookies.zdme.controller;

import lombok.RequiredArgsConstructor;
import org.rookies.zdme.model.dto.PaymentCancelDto;
import org.rookies.zdme.model.dto.PaymentRequestDto;
import org.rookies.zdme.model.dto.PaymentResponseDto;
import org.rookies.zdme.model.entity.Payment;
import org.rookies.zdme.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // 결제 요청 (사용자)
    @PostMapping("/user/confirm")
    public ResponseEntity<?> confirmPayment(@RequestBody PaymentRequestDto reqDto) {
        try {
            PaymentResponseDto resDto = paymentService.tossPaymentConfirm(reqDto);

            return ResponseEntity.ok().body(resDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // 내 결제 내역 조회 (사용자)
    @GetMapping("/user")
    public ResponseEntity<?> getMyPayments() {
        List<Payment> payments = paymentService.getPayments();

        return ResponseEntity.ok(payments);
    }

    // 결제 취소 (관리자)
    @PostMapping("/admin/cancel")
    public ResponseEntity<?> cancelPayment(@RequestBody PaymentCancelDto dto) {
        Payment canceledPayment = paymentService.cancelPayment(dto);

        Map<String, Object> response = new HashMap<>();
        response.put("paymentKey", canceledPayment.getPaymentKey());
        response.put("orderId", canceledPayment.getOrderId());
        response.put("status", canceledPayment.getPaymentStatus());
        response.put("canceledAmount", canceledPayment.getAmount());

        return ResponseEntity.ok(response);
    }
}
