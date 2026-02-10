package org.rookies.zdme.service;

import org.rookies.zdme.exception.BadRequestException;
import org.rookies.zdme.model.entity.SecurityTempCache;
import org.rookies.zdme.repository.SecurityTempCacheRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class SecurityService {

    private final SecurityTempCacheRepository securityTempCacheRepository;

    @Value("${app.integrity.canonical-binary-hash}")
    private String canonicalBinaryHash;

    @Value("${app.integrity.canonical-signature-hash}")
    private String canonicalSignatureHash;

    public SecurityService(SecurityTempCacheRepository securityTempCacheRepository) {
        this.securityTempCacheRepository = securityTempCacheRepository;
    }

    @Transactional
    public String generateNonce() {
        String nonce = UUID.randomUUID().toString();
        // TTL 60 seconds
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(60);
        SecurityTempCache securityTempCache = new SecurityTempCache(nonce, null, expiresAt);
        securityTempCacheRepository.save(securityTempCache);
        return nonce;
    }

    @Transactional
    public String verifyIntegrityAndIssueToken(String nonce, String binaryHash, String signatureHash, String deviceId) {
        // 1. Nonce 검증: DB 존재 여부 및 만료 확인 -> 확인 즉시 삭제(Delete-on-Read)
        Optional<SecurityTempCache> optionalNonce = securityTempCacheRepository.findValidCacheByKey(nonce, LocalDateTime.now());
        if (optionalNonce.isEmpty()) {
            throw new BadRequestException("Invalid or expired nonce.");
        }
        securityTempCacheRepository.deleteByKey(nonce); // Delete-on-Read

        // 2. 무결성 판단: 요청된 해시값들이 서버에 저장된 '정본 해시값'과 일치하는지 비교
        if (!canonicalBinaryHash.equals(binaryHash) || !canonicalSignatureHash.equals(signatureHash)) {
            throw new BadRequestException("App integrity verification failed. Hashes do not match.");
        }

        // 3. 토큰 발급: 통과 시 integrity_token 생성 -> DB 저장 (Key: 토큰, Value: device_id, TTL 180초)
        String integrityToken = UUID.randomUUID().toString();
        // TTL 180 seconds
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(180);
        SecurityTempCache integrityTokenCache = new SecurityTempCache(integrityToken, deviceId, expiresAt);
        securityTempCacheRepository.save(integrityTokenCache);

        return integrityToken;
    }

    @Transactional
    public boolean validateIntegrityToken(String integrityToken, String deviceId) {
        // 1. 오직 Key로만 먼저 조회 (시간 조건 제거)
        Optional<SecurityTempCache> optionalToken = securityTempCacheRepository.findById(integrityToken);

        if (optionalToken.isEmpty()) {
            System.out.println("DEBUG: 토큰이 DB에 존재하지 않음 -> " + integrityToken);
            return false;
        }

        SecurityTempCache storedToken = optionalToken.get();

        // 2. 시간 검증 (자바 코드에서 수행)
        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            System.out.println("DEBUG: 토큰 만료됨. 만료시간: " + storedToken.getExpiresAt() + ", 현재시간: " + LocalDateTime.now());
            securityTempCacheRepository.delete(storedToken); // 만료된 건 지워줌
            return false;
        }

        // 3. Device ID 일치 확인
        if (!deviceId.equals(storedToken.getCacheValue())) {
            System.out.println("DEBUG: 기기 정보 불일치. 저장값: " + storedToken.getCacheValue() + ", 요청값: " + deviceId);
            return false;
        }

        // 4. 검증 통과 후 즉시 삭제 (재사용 방지)
        securityTempCacheRepository.delete(storedToken);
        return true;
    }
}
