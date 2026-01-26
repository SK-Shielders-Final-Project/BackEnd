package org.rookies.zdme.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.boot.model.naming.IllegalIdentifierException;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Builder
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 100)
    private String orderId;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false)
    private Long remainAmount;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentStatus paymentStatus;

    @Column(length = 50)
    private String paymentMethod;

    // toss 발급 결제 고유 키
    @Column(length = 100)
    private String paymentKey;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum PaymentStatus{
        READY, DONE, CANCELED, PARTIAL_CANCELED
    }

    public void cancelPayment(Long cancelAmount) {
        // 환불하려는 금액이 더 많은 경우
        if (this.remainAmount < cancelAmount) {
            throw new IllegalIdentifierException("환불 요청이 남은 잔액보다 큽니다.");
        }
        this.remainAmount -= cancelAmount;

        if(this.remainAmount == 0) {
            this.paymentStatus = PaymentStatus.CANCELED;
        } else {
            this.paymentStatus = PaymentStatus.PARTIAL_CANCELED;
        }
    }
}
