package org.example.signer.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service for issuing, parsing, and validating tenant-aware JSON Web Tokens (JWT).
 */
@Slf4j
@Service
public class JwtService {

    @Value("${jwt.secret:${app.jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}}")
    private String secretKey;

    @Value("${jwt.expiration:${app.jwt.expiration-ms:86400000}}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration:604800000}") // 7 days default
    private long refreshExpiration;

    private Key getSignInKey() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secretKey);
        } catch (IllegalArgumentException e) {
            keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(User user, String tenantSlug) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("email", user.getEmail());
        extraClaims.put("tenantId", user.getTenantId());
        extraClaims.put("tenantSlug", tenantSlug);
        extraClaims.put("role", user.getRole() != null ? user.getRole().name() : "VIEWER");
        extraClaims.put("token_type", "ACCESS");

        String subject = user.getUserUuid() != null ? user.getUserUuid().toString() : user.getEmail();
        return buildToken(extraClaims, subject, jwtExpiration);
    }

    public String generateRefreshToken(User user, String tenantSlug) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("email", user.getEmail());
        extraClaims.put("tenantId", user.getTenantId());
        extraClaims.put("tenantSlug", tenantSlug);
        extraClaims.put("role", user.getRole() != null ? user.getRole().name() : "VIEWER");
        extraClaims.put("token_type", "REFRESH");

        String subject = user.getUserUuid() != null ? user.getUserUuid().toString() : user.getEmail();
        return buildToken(extraClaims, subject, refreshExpiration);
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, long expiration) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractTenantId(String token) {
        return extractClaim(token, claims -> {
            Object val = claims.get("tenantId");
            if (val instanceof Number) {
                return ((Number) val).longValue();
            }
            return null;
        });
    }

    public String extractTenantSlug(String token) {
        return extractClaim(token, claims -> claims.get("tenantSlug", String.class));
    }

    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("token_type", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        if (userDetails instanceof TenantUserDetails) {
            TenantUserDetails tenantUserDetails = (TenantUserDetails) userDetails;
            Long tokenTenantId = extractTenantId(token);
            return username.equals(userDetails.getUsername())
                    && (tokenTenantId == null || tokenTenantId.equals(tenantUserDetails.getTenantId()))
                    && !isTokenExpired(token);
        }
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean isTokenValid(String token, User user) {
        final String username = extractUsername(token);
        final Long tenantId = extractTenantId(token);
        String expectedIdentifier = user.getUserUuid() != null ? user.getUserUuid().toString() : user.getEmail();

        return username.equals(expectedIdentifier)
                && (tenantId == null || tenantId.equals(user.getTenantId()))
                && !isTokenExpired(token);
    }

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        return expiration != null && expiration.before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public long getExpirationMs() {
        return jwtExpiration;
    }

    public long getRefreshExpirationMs() {
        return refreshExpiration;
    }
}
