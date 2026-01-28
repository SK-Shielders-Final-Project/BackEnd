package org.rookies.zdme.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtUtil implements Serializable {

    private static final long serialVersionUID = -2550185165626007488L;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${jwt.refresh-expiration-ms}")
    private long jwtRefreshExpirationMs;

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private String keyId;

    /**
     * RS256 알고리즘을 위한 RSA 키 쌍을 생성(운영X)
     */
    @PostConstruct
    public void init() {
        KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();
        this.keyId = UUID.randomUUID().toString();
    }

    /**
     * 표준 JWK 형식으로 공개키 반환
     * @return JWK 형식의 Map
     */
    public Map<String, Object> getJwk() {
        Map<String, Object> jwk = new HashMap<>();
        RSAPublicKey rsaPublicKey = (RSAPublicKey) this.publicKey;
        jwk.put("kty", "RSA");
        jwk.put("use", "sig");
        jwk.put("kid", this.keyId);
        jwk.put("alg", "RS256");
        jwk.put("n", Base64.getUrlEncoder().withoutPadding().encodeToString(rsaPublicKey.getModulus().toByteArray()));
        jwk.put("e", Base64.getUrlEncoder().withoutPadding().encodeToString(rsaPublicKey.getPublicExponent().toByteArray()));
        return jwk;
    }

    // JWT 토큰에서 사용자 이름 추출
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    // JWT 토큰에서 만료 날짜 추출
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 토큰에서 정보 추출 (공개키 이용)
     */
    private Claims getAllClaimsFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                throw new MalformedJwtException("JWT 토큰은 최소 2개의 부분으로 구성되어야 합니다.");
            }
            String headerPart = new String(Base64.getUrlDecoder().decode(parts[0]));

            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> headerMap = mapper.readValue(headerPart, new TypeReference<Map<String, String>>() {});
            String alg = headerMap.get("alg");

            if (alg == null) {
                throw new MalformedJwtException("JWT 헤더에 'alg' 필드가 없습니다.");
            }

            if (SignatureAlgorithm.HS256.getValue().equals(alg)) {
                // 취약한 경로: alg가 HS256인 경우, 공개키를 HMAC 비밀키로 간주
                Key hmacKey = Keys.hmacShaKeyFor(publicKey.getEncoded());
                return Jwts.parserBuilder()
                        .setSigningKey(hmacKey)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
            } else {
                return Jwts.parserBuilder()
                        .setSigningKey(publicKey)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
            }
        } catch (JsonProcessingException e) {
            throw new MalformedJwtException("JWT 헤더 JSON 파싱 오류", e);
        } catch (Exception e) {
            throw new RuntimeException("유효하지 않은 JWT 토큰", e);
        }
    }

    /**
     * 토큰이 만료되었는지 확인
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * 사용자를 위한 토큰 생성
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, userDetails.getUsername());
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateRefreshToken(claims, userDetails.getUsername());
    }

    /**
     * JWT 토큰 생성
     * @param claims
     * @param subject
     * @return
     */
    private String doGenerateToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setHeaderParam("kid", this.keyId)
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(privateKey, SignatureAlgorithm.RS256).compact();
    }

    private String doGenerateRefreshToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setHeaderParam("kid", this.keyId)
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtRefreshExpirationMs))
                .signWith(privateKey, SignatureAlgorithm.RS256).compact();
    }

    /**
     * 토큰 검증
     * @param token
     * @param userDetails
     * @return
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
