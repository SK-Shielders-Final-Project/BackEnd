package org.rookies.zdme.repository;

import org.rookies.zdme.model.entity.SecurityTempCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface SecurityTempCacheRepository extends JpaRepository<SecurityTempCache, String> {

    @Query("SELECT stc FROM SecurityTempCache stc WHERE stc.cacheKey = :cacheKey AND stc.expiresAt > :currentTime")
    Optional<SecurityTempCache> findValidCacheByKey(@Param("cacheKey") String cacheKey, @Param("currentTime") LocalDateTime currentTime);

    @Modifying
    @Query("DELETE FROM SecurityTempCache stc WHERE stc.cacheKey = :cacheKey")
    void deleteByKey(@Param("cacheKey") String cacheKey);
}
