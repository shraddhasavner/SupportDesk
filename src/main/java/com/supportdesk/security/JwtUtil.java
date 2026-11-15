package com.supportdesk.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.supportdesk.entity.Employee;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JwtUtil: Utility class for generating, signing, and validating JSON Web Tokens (JWT).
 * 
 * [English]:
 * - Uses HMAC256 with a configurable secret key.
 * - Embeds user email (subject), employee ID, name, and role into token claims.
 * - Validates cryptographic signature and expiration timestamp.
 * - Secret key is read from application properties (app.jwt.secret) and can be overridden via environment variables.
 * - Never logs passwords, tokens, or signing secrets.
 * 
 * [Hinglish]:
 * - JWT token create karne aur verify karne ke liye utility class.
 * - Token ke andar user ka email, ID aur role claims ke roop me rehte hain.
 * - Token expired ho ya signature invalid ho, toh verify method exception throw karta hai.
 */
@Component
public class JwtUtil {

    private final String secret;
    private final long expirationMs;

    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms:86400000}") long expirationMs) {
        if (secret == null || secret.trim().isEmpty() || secret.trim().length() < 32) {
            throw new IllegalArgumentException("JWT secret key must be configured and be at least 256 bits (32 characters) long.");
        }
        this.secret = secret.trim();
        this.expirationMs = expirationMs;
    }

    /**
     * Computes a deterministic, one-way HMAC-SHA256 fingerprint of the user's password hash.
     * Never stores, prints, or exposes raw passwords or BCrypt hashes in JWTs or logs.
     */
    public String computePasswordFingerprint(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            return "";
        }
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secretKeySpec =
                    new javax.crypto.spec.SecretKeySpec(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hmac = mac.doFinal(passwordHash.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hmac).substring(0, 32);
        } catch (Exception ex) {
            try {
                java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
                md.update((passwordHash + secret).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                return java.util.HexFormat.of().formatHex(md.digest()).substring(0, 32);
            } catch (Exception e) {
                return String.valueOf((passwordHash + secret).hashCode());
            }
        }
    }

    /**
     * Generates a signed JWT token containing employee details.
     * Includes a one-way password state fingerprint to allow invalidation upon password changes.
     */
    public String generateToken(Employee employee) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        String pwdFingerprint = computePasswordFingerprint(employee.getPassword());

        com.auth0.jwt.JWTCreator.Builder builder = JWT.create()
                .withSubject(employee.getEmail())
                .withClaim("id", employee.getId())
                .withClaim("name", employee.getName())
                .withClaim("role", employee.getRole() != null ? employee.getRole().name() : "")
                .withIssuedAt(now)
                .withExpiresAt(expiryDate);

        if (pwdFingerprint != null && !pwdFingerprint.isBlank()) {
            builder.withClaim("pwd", pwdFingerprint);
        }

        return builder.sign(algorithm);
    }

    /**
     * Validates signature and expiration of the JWT.
     * Throws JWTVerificationException if signature is invalid or token has expired.
     */
    public DecodedJWT validateToken(String token) throws JWTVerificationException {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTVerifier verifier = JWT.require(algorithm).build();
        return verifier.verify(token);
    }

    /**
     * Extracts email (subject) from validated JWT.
     */
    public String getEmailFromToken(String token) {
        return validateToken(token).getSubject();
    }

    /**
     * Extracts employee ID claim from validated JWT.
     */
    public Long getIdFromToken(String token) {
        return validateToken(token).getClaim("id").asLong();
    }

    /**
     * Extracts user role claim from validated JWT.
     */
    public String getRoleFromToken(String token) {
        return validateToken(token).getClaim("role").asString();
    }

    /**
     * Verifies that the JWT was issued for the employee's current password state.
     * If the password has changed since the token was issued, the token is invalidated.
     */
    public boolean isTokenValidForPassword(String token, String currentPasswordHash) {
        try {
            DecodedJWT decoded = validateToken(token);
            String tokenPwdClaim = decoded.getClaim("pwd").asString();
            if (tokenPwdClaim != null && !tokenPwdClaim.isBlank()) {
                String currentFingerprint = computePasswordFingerprint(currentPasswordHash);
                return tokenPwdClaim.equals(currentFingerprint);
            }
            return true;
        } catch (JWTVerificationException ex) {
            return false;
        }
    }
}
