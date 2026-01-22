package org.rookies.zdme.model.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.rookies.zdme.model.entity.User;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
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
        READY, DONE, CANCELED, ABORTED
    }

    @Builder
    public Payment(User user, String orderId, Long amount) {
        this.user = user;
        this.orderId = orderId;
        this.amount = amount;
        this.paymentStatus = PaymentStatus.READY;
    }

    public void confirmPaymentSuccess(String paymentKey, String paymentMethod){
        this.paymentKey = paymentKey;
        this.paymentStatus = PaymentStatus.DONE;
        this.paymentMethod = paymentMethod;
    }
}
