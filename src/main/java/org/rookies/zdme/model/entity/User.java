package org.rookies.zdme.model.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "card_number", length = 20)
    private String cardNumber;

    @Column(name = "total_point")
    private Long totalPoint;

    @Column(name = "pass", length = 100)
    private String pass;

    @Column(name = "admin_level", nullable = true)
    private Integer adminLevel;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (totalPoint == null) totalPoint = 0L;
        if (adminLevel == null) adminLevel = 0;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
