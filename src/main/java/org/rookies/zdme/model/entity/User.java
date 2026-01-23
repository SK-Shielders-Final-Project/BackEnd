package org.rookies.zdme.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 20)
    private String cardNumber;

    @Column
    private Long totalPoint;

    @Column(length = 100)
    private String pass;

    @Column
    private Integer adminLevel;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    public void updatePoint(Long amount) {
        // 포인트가 -가 되는 것을 방지 (비즈니스 로직 취약점)
//        if (amount + this.totalPoint < 0) {
//            throw new IllegalStateException("회수할 포인트가 부족합니다.");
//        }
        this.totalPoint += amount;
    }
}
