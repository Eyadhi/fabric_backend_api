package com.example.fabric.util;

import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
public class JwtUtil {
    private static final String SECRET_KEY = "asdgfhgjhuytyrtrarszhfjjhgfsszdxhfjaszdtxfhycgjnggdfgcgvnh";

    public String generateToken(String username, int roleId) {
        return Jwts.builder()
                .setSubject(username)
                .claim("roleId", roleId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24 hour expiration
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // Method to extract username from token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Method to extract roleId from token
    public Integer extractRoleId(String token) {
        return extractClaim(token, claims -> claims.get("roleId", Integer.class));
    }

    // Extract all claims from the token
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    // Extract claim using a function
    private <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extract expiration date from token
    private java.util.Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Enhanced token validation with specific error messages
    public TokenValidationResult validateTokenWithDetails(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return new TokenValidationResult(false, "Token is missing", TokenErrorType.INVALID);
            }

            Claims claims = extractAllClaims(token);
            
            // Check if token is expired
            if (claims.getExpiration().before(new Date())) {
                return new TokenValidationResult(false, "Token expired", TokenErrorType.EXPIRED);
            }
            
            return new TokenValidationResult(true, "Token is valid", TokenErrorType.NONE);
            
        } catch (ExpiredJwtException e) {
            return new TokenValidationResult(false, "Token expired", TokenErrorType.EXPIRED);
        } catch (MalformedJwtException e) {
            return new TokenValidationResult(false, "Invalid token format", TokenErrorType.INVALID);
        } catch (SignatureException e) {
            return new TokenValidationResult(false, "Invalid token signature", TokenErrorType.INVALID);
        } catch (IllegalArgumentException e) {
            return new TokenValidationResult(false, "Token is invalid", TokenErrorType.INVALID);
        } catch (Exception e) {
            return new TokenValidationResult(false, "Token validation failed", TokenErrorType.INVALID);
        }
    }

    // Enhanced username extraction with error handling
    public String extractUsernameWithValidation(String token) throws JwtException {
        try {
            return extractUsername(token);
        } catch (ExpiredJwtException e) {
            throw new ExpiredJwtException(e.getHeader(), e.getClaims(), "Token expired");
        } catch (Exception e) {
            throw new JwtException("Invalid token");
        }
    }

    // Token validation result class
    public static class TokenValidationResult {
        private final boolean valid;
        private final String message;
        private final TokenErrorType errorType;

        public TokenValidationResult(boolean valid, String message, TokenErrorType errorType) {
            this.valid = valid;
            this.message = message;
            this.errorType = errorType;
        }

        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public TokenErrorType getErrorType() { return errorType; }
    }

    // Token error types
    public enum TokenErrorType {
        NONE,
        EXPIRED,
        INVALID
    }

    // Public method to check if token is expired (for enhanced validation)
    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new java.util.Date());
        } catch (Exception e) {
            return true; // Assume expired if we can't parse
        }
    }

    public Date getExpirationDateFromToken(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
