package com.paperless.rest.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class JwtService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private final String secretKey;
    private final long expirationMs;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtService(
            @Value("${jwt.secret:paperless-super-secret-key-for-swen3-semester-project-2026}") String secretKey,
            @Value("${jwt.expiration-ms:86400000}") long expirationMs) {
        this.secretKey = secretKey;
        this.expirationMs = expirationMs;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    /**
     * Generates a signed RFC 7519 HMAC-SHA256 JWT token for the user.
     */
    public String generateToken(String username, String role) {
        try {
            long now = Instant.now().getEpochSecond();
            long exp = now + (expirationMs / 1000);

            Map<String, Object> header = new HashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");

            Map<String, Object> payload = new HashMap<>();
            payload.put("sub", username);
            payload.put("role", role);
            payload.put("iat", now);
            payload.put("exp", exp);

            String encodedHeader = Base64.getUrlEncoder().withoutPadding().encodeToString(objectMapper.writeValueAsBytes(header));
            String encodedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(objectMapper.writeValueAsBytes(payload));
            String dataToSign = encodedHeader + "." + encodedPayload;

            String signature = sign(dataToSign);
            return dataToSign + "." + signature;
        } catch (Exception e) {
            log.error("Failed to generate JWT token for user {}", username, e);
            throw new IllegalStateException("JWT generation error", e);
        }
    }

    /**
     * Validates signature, structure, and expiration time of the JWT token.
     */
    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return false;
        }

        try {
            String dataToSign = parts[0] + "." + parts[1];
            String expectedSignature = sign(dataToSign);

            if (!MessageDigest.isEqual(parts[2].getBytes(StandardCharsets.UTF_8), expectedSignature.getBytes(StandardCharsets.UTF_8))) {
                return false;
            }

            Map<String, Object> payload = parsePayload(parts[1]);
            Number expNumber = (Number) payload.get("exp");
            if (expNumber == null) {
                return false;
            }

            long exp = expNumber.longValue();
            long now = Instant.now().getEpochSecond();
            return now <= exp;
        } catch (Exception e) {
            log.debug("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extracts username (sub claim) from the token.
     */
    public String extractUsername(String token) {
        Map<String, Object> payload = extractPayload(token);
        return payload != null ? (String) payload.get("sub") : null;
    }

    /**
     * Extracts user role from the token.
     */
    public String extractRole(String token) {
        Map<String, Object> payload = extractPayload(token);
        return payload != null ? (String) payload.get("role") : null;
    }

    private Map<String, Object> extractPayload(String token) {
        if (token == null) return null;
        String[] parts = token.split("\\.");
        if (parts.length != 3) return null;
        try {
            return parsePayload(parts[1]);
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, Object> parsePayload(String encodedPayload) throws Exception {
        byte[] decoded = Base64.getUrlDecoder().decode(encodedPayload);
        return objectMapper.readValue(decoded, new TypeReference<Map<String, Object>>() {});
    }

    private String sign(String data) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
        mac.init(secretKeySpec);
        byte[] signedBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(signedBytes);
    }
}
