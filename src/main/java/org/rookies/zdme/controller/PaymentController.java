package org.rookies.zdme.controller;

import lombok.RequiredArgsConstructor;
import org.apache.catalina.connector.Response;
import org.rookies.zdme.model.dto.PaymentCancelDto;
import org.rookies.zdme.model.dto.PaymentSuccessDto;
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

    @PostMapping("/user/confirm")
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

    @GetMapping("/user")
    public ResponseEntity<?> getMyPayments() {
        List<Payment> payments = paymentService.getPayments();

        return ResponseEntity.ok(payments);
    }

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
