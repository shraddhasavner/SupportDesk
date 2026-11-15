package com.supportdesk.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.supportdesk.entity.Employee;
import com.supportdesk.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtUtilTests
{
    private JwtUtil jwtUtil;
    private final String secret = "TestSecretKeyForJwtSigningMustBeAtLeast256BitsLong2026!TestSecret";
    private final long expirationMs = 3600000;

    @BeforeEach
    void setUp()
    {
        jwtUtil = new JwtUtil(secret, expirationMs);
    }

    @Test
    @DisplayName("Should generate valid JWT token containing employee claims")
    void testGenerateAndValidateToken()
    {
        Employee employee = new Employee("Karan Mehra", "karan@test.com", "password", "IT", Role.TECHNICIAN, "12345");
        employee.setId(10L);

        String token = jwtUtil.generateToken(employee);
        assertNotNull(token);

        DecodedJWT decoded = jwtUtil.validateToken(token);
        assertEquals("karan@test.com", decoded.getSubject());
        assertEquals(10L, decoded.getClaim("id").asLong());
        assertEquals("Karan Mehra", decoded.getClaim("name").asString());
        assertEquals("TECHNICIAN", decoded.getClaim("role").asString());
    }

    @Test
    @DisplayName("Should extract subject, ID, and role from token")
    void testExtractClaims()
    {
        Employee employee = new Employee("Admin User", "admin@supportdesk.com", "password", "IT", Role.ADMIN, "999");
        employee.setId(1L);

        String token = jwtUtil.generateToken(employee);

        assertEquals("admin@supportdesk.com", jwtUtil.getEmailFromToken(token));
        assertEquals(1L, jwtUtil.getIdFromToken(token));
        assertEquals("ADMIN", jwtUtil.getRoleFromToken(token));
    }

    @Test
    @DisplayName("Should reject token signed with different secret key")
    void testRejectInvalidSignature()
    {
        Employee employee = new Employee("User", "user@test.com", "password", "HR", Role.EMPLOYEE, "111");
        employee.setId(5L);

        String token = jwtUtil.generateToken(employee);

        JwtUtil differentSecretUtil = new JwtUtil("DifferentSecretKeyMustBeAtLeast256BitsLong2026!WrongKey", expirationMs);
        assertThrows(JWTVerificationException.class, () -> differentSecretUtil.validateToken(token));
    }

    @Test
    @DisplayName("Should reject expired token")
    void testRejectExpiredToken()
    {
        JwtUtil expiredUtil = new JwtUtil(secret, -1000);

        Employee employee = new Employee("Expired User", "expired@test.com", "password", "Sales", Role.EMPLOYEE, "222");
        employee.setId(2L);

        String expiredToken = expiredUtil.generateToken(employee);
        assertThrows(JWTVerificationException.class, () -> jwtUtil.validateToken(expiredToken));
    }

    @Test
    @DisplayName("Should reject null, empty, or secret keys shorter than 32 characters (256 bits)")
    void testRejectInvalidSecretLength()
    {
        assertThrows(IllegalArgumentException.class, () -> new JwtUtil(null, expirationMs));
        assertThrows(IllegalArgumentException.class, () -> new JwtUtil("", expirationMs));
        assertThrows(IllegalArgumentException.class, () -> new JwtUtil("   ", expirationMs));
        assertThrows(IllegalArgumentException.class, () -> new JwtUtil("shortKey123", expirationMs));
        assertThrows(IllegalArgumentException.class, () -> new JwtUtil("lessThanThirtyTwoCharacters!!", expirationMs));
    }

    @Test
    @DisplayName("Should include password fingerprint claim in token")
    void testTokenIncludesPasswordFingerprint()
    {
        Employee employee = new Employee("Fingerprint User", "fp@test.com", "SecretPass123", "IT", Role.EMPLOYEE, "333");
        employee.setId(3L);

        String token = jwtUtil.generateToken(employee);
        DecodedJWT decoded = jwtUtil.validateToken(token);

        String pwdClaim = decoded.getClaim("pwd").asString();
        assertNotNull(pwdClaim);
        assertFalse(pwdClaim.isBlank());
        assertEquals(32, pwdClaim.length(), "Fingerprint must be 32-character hex");
    }

    @Test
    @DisplayName("Should validate token against unchanged password hash")
    void testTokenValidForMatchingPassword()
    {
        Employee employee = new Employee("Matching User", "match@test.com", "$2a$10$abcdef1234567890", "IT", Role.EMPLOYEE, "444");
        employee.setId(4L);

        String token = jwtUtil.generateToken(employee);
        assertTrue(jwtUtil.isTokenValidForPassword(token, "$2a$10$abcdef1234567890"));
    }

    @Test
    @DisplayName("Should reject token when password hash has changed")
    void testTokenInvalidWhenPasswordHashChanges()
    {
        Employee employee = new Employee("Changed User", "changed@test.com", "$2a$10$oldPasswordHash123", "IT", Role.EMPLOYEE, "555");
        employee.setId(5L);

        String token = jwtUtil.generateToken(employee);

        assertTrue(jwtUtil.isTokenValidForPassword(token, "$2a$10$oldPasswordHash123"));
        assertFalse(jwtUtil.isTokenValidForPassword(token, "$2a$10$newPasswordHash456"));
    }
}