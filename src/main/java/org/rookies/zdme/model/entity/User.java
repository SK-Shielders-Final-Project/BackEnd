package org.rookies.zdme.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

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

    @Column
    private Long totalPoint;

    @Column
    private Integer adminLevel;

    @Column(name = "card_number", length = 50)
    private String cardNumber;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateInfo(String name, String email, String phone, Integer adminLevel) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        if (adminLevel != null) {
            this.adminLevel = adminLevel;
        }
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.adminLevel != null) {
            if (this.adminLevel == 2) {
                return List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
            } else if (this.adminLevel == 1) {
                return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
            }
        }
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Column(length = 512)
    private String refreshToken;

    public void updatePoint(Long amount) {
        // 포인트가 -가 되는 것을 방지 (비즈니스 로직 취약점)
//        if (amount + this.totalPoint < 0) {
//            throw new IllegalStateException("회수할 포인트가 부족합니다.");
//        }
        this.totalPoint += amount;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

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

    public void setAdminLevel(Integer adminLevel) {
        this.adminLevel = adminLevel;
    }

}
