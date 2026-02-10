package org.rookies.zdme.model.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "SECURITY_TEMP_CACHE")
@Getter
@NoArgsConstructor
public class SecurityTempCache {

    @Id
    @Column(name = "CACHE_KEY", nullable = false, unique = true)
    private String cacheKey;

    @Column(name = "CACHE_VALUE", nullable = true)
    private String cacheValue;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "EXPIRES_AT", nullable = false)
    private LocalDateTime expiresAt;

    public SecurityTempCache(String cacheKey, String cacheValue, LocalDateTime expiresAt) {
        this.cacheKey = cacheKey;
        this.cacheValue = cacheValue;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    // Intention-revealing method for updating cacheValue
    public void updateCacheValue(String newCacheValue) {
        this.cacheValue = newCacheValue;
    }
}
