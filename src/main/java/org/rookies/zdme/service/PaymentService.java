package org.rookies.zdme.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.rookies.zdme.model.dto.PaymentCancelDto;
import org.rookies.zdme.model.dto.PaymentRequestDto;
import org.rookies.zdme.model.dto.PaymentResponseDto;
import org.rookies.zdme.model.dto.PaymentsDto;
import org.rookies.zdme.model.entity.Payment;
import org.rookies.zdme.model.entity.User;
import org.rookies.zdme.repository.PaymentRepository;
import org.rookies.zdme.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {

    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    @Value("${toss.secret-key}")
    private String tossSecretKey;

    // toss로 결제 요청 및 승인
    public PaymentResponseDto tossPaymentConfirm(PaymentRequestDto dto) {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        // 인증 헤더 설정
        String encodedAuth = Base64.getEncoder()
                .encodeToString((tossSecretKey + ":").getBytes(StandardCharsets.UTF_8));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Basic " + encodedAuth);

        // toss로 보낼 request 값 생성
        Map<String, Object> params = new HashMap<>();
        params.put("paymentKey", dto.getPaymentKey());
        params.put("orderId", dto.getOrderId());
        params.put("amount", dto.getAmount());

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(params, headers);

        try {
            // toss 결제 요청 request 전송
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://api.tosspayments.com/v1/payments/confirm",
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            // 시큐어 코딩 버전 (토스에서 넘어온 response 값을 기준으로 payment 생성)
//            JsonNode jsonNode = objectMapper.readTree(response.getBody());
//            String approvedId = jsonNode.get("orderId").asText();
//            String approvedKey = jsonNode.get("paymentKey").asText();
//            Long approvedAmount = jsonNode.get("totalAmount").asLong();

            // userId 값 고정. jwt 수정 예정
            Long userId = 3L;
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

            // 사용자가 입력한 정보를 바탕으로 payment 객체 생성 (취약점 존재)
            Payment payment = Payment.builder()
                    .user(user)
                    .orderId(dto.getOrderId())
                    .amount(dto.getAmount())
                    .paymentKey(dto.getPaymentKey())
                    .paymentStatus(Payment.PaymentStatus.DONE)
                    .paymentMethod("카드")
                    .remainAmount(dto.getAmount())
                    .build();
            user.updatePoint(dto.getAmount());

            // DB 내용 저장
            paymentRepository.save(payment);

            return PaymentResponseDto.builder()
                    .paymentId(payment.getPaymentId())
                    .userId(user.getUserId())
                    .totalPoint(user.getTotalPoint())
                    .paymentKey(payment.getPaymentKey())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("토스 결제 승인 실패: " + e.getMessage());
        }
    }

    // 결제 내역 확인
    public List<PaymentsDto> getPayments() {
        // userId를 고정 -> 추후에 jwt 기반으로 변경할 예정
        Long userId = 3L;
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자가 존재하지 않습니다."));

        List<Payment> payments = paymentRepository.findAllByUser(user);

        return payments.stream()
                .map(p -> PaymentsDto.builder()
                        .orderId(p.getOrderId())
                        .paymentKey(p.getPaymentKey())
                        .paymentMethod(p.getPaymentMethod())
                        .createAt(p.getCreatedAt())
                        .amount(p.getAmount())
                        .remainAmount(p.getRemainAmount())
                        .userId(p.getUser().getUserId())
                        .status(p.getPaymentStatus().toString())
                        .build())
                .collect(Collectors.toList());
    }

    // 환불
    public Payment cancelPayment(PaymentCancelDto dto) {
        // 유효한 PaymentKey 인지 확인 후 해당 결제 내역 불러오기
        // toss payments에서 PaymentKey를 기반으로 결제를 취소함
        Payment payment = paymentRepository.findByPaymentKey(dto.getPaymentKey())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 결제 내역입니다."));

        User user = payment.getUser();

        // 결제 상태가 취소인 경우
        if (payment.getPaymentStatus() == Payment.PaymentStatus.CANCELED) {
            throw new RuntimeException("이미 취소된 결제입니다.");
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        String encodedAuth = Base64.getEncoder()
                .encodeToString((tossSecretKey + ":").getBytes(StandardCharsets.UTF_8));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Basic " + encodedAuth);

        Map<String, Object> params = new HashMap<>();
        params.put("cancelReason", dto.getCancelReason());
        params.put("cancelAmount", dto.getCancelAmount());

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(params, headers);

        try {
            String url = "https://api.tosspayments.com/v1/payments/" + dto.getPaymentKey() + "/cancel";

            restTemplate.postForEntity(url, requestEntity, String.class);

            // [취약점] 포인트 회수 로직 누락
            // 부분 환불하면 환불한 만큼의 포인트를 차감해야 함
            // 아래의 로직을 사용하는 경우 point가 마이너스가 될 수 있음
            user.updatePoint(-1 * dto.getCancelAmount());

            payment.cancelPayment(dto.getCancelAmount());
            return paymentRepository.save(payment);
        } catch (Exception e) {
            throw new RuntimeException("환불 실패: " + e.getMessage());
        }
    }
}
