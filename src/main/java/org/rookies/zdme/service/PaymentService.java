package org.rookies.zdme.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.rookies.zdme.model.dto.PaymentSuccessDto;
import org.rookies.zdme.model.entity.Payment;
import org.rookies.zdme.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Value("${toss.secret-key}")
    private String tossSecretKey;

    public Payment tossPaymentConfirm(PaymentSuccessDto dto) {
        Payment payment = paymentRepository.findByOrderId(dto.getOrderId())
                .orElseThrow(() -> new RuntimeException("주문 정보를 찾을 수 없습니다."));

        if (!payment.getAmount().equals(dto.getAmount())) {
            throw new RuntimeException("결제 금액이 일치하지 않습니다.");
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        String encodedAuth = Base64.getEncoder()
                .encodeToString((tossSecretKey + ":").getBytes(StandardCharsets.UTF_8));
        headers.setBasicAuth(encodedAuth);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        Map<String, Object> params = new HashMap<>();
        params.put("paymentKey", dto.getPaymentKey());
        params.put("orderId", dto.getOrderId());
        params.put("amount", dto.getAmount());

        HttpEntity<String> requestEntity = new HttpEntity<>(params.toString(), headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://api.tosspayments.com/v1/payments/confirm",
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );
            payment.confirmPaymentSuccess(dto.getPaymentKey(), "카드");
            return payment;
        } catch (Exception e) {
            throw new RuntimeException("토스 결제 승인 실패: " + e.getMessage());
        }
    }
}
