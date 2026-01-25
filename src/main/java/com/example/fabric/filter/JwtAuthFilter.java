package com.example.fabric.filter;

import com.example.fabric.model.User;
import com.example.fabric.repository.UserRepository;
import com.example.fabric.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public JwtAuthFilter(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/auth/register") || path.startsWith("/auth/login") || path.startsWith("/error");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // Skip filtering for permitAll endpoints
        if (shouldNotFilter(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);
        
        try {
            // Validate token with detailed error information
            JwtUtil.TokenValidationResult validationResult = jwtUtil.validateTokenWithDetails(token);
            
            if (!validationResult.isValid()) {
                sendErrorResponse(response, validationResult.getMessage(), validationResult.getErrorType());
                return;
            }

            final String username = jwtUtil.extractUsername(token);
            final Integer roleId = jwtUtil.extractRoleId(token);

            if (username != null && roleId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = userRepository.findByUsername(username).orElse(null);

                if (user != null && token.equals(user.getToken()) && user.getTokenExpiry().isAfter(LocalDateTime.now())) {
                    // Use roleId from JWT token to determine role name
                    String roleName = getRoleNameFromId(roleId);
                    var authorities = Collections
                            .singletonList(new SimpleGrantedAuthority("ROLE_" + roleName.toUpperCase()));

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, null,
                            authorities);
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    // User not found or token doesn't match or custom expiry passed
                    if (user == null) {
                        sendErrorResponse(response, "Invalid token", JwtUtil.TokenErrorType.INVALID);
                    } else if (!token.equals(user.getToken())) {
                        sendErrorResponse(response, "Invalid token", JwtUtil.TokenErrorType.INVALID);
                    } else if (!user.getTokenExpiry().isAfter(LocalDateTime.now())) {
                        sendErrorResponse(response, "Token expired", JwtUtil.TokenErrorType.EXPIRED);
                    }
                    return;
                }
            }

        } catch (ExpiredJwtException e) {
            sendErrorResponse(response, "Token expired", JwtUtil.TokenErrorType.EXPIRED);
            return;
        } catch (JwtException e) {
            sendErrorResponse(response, "Invalid token", JwtUtil.TokenErrorType.INVALID);
            return;
        } catch (Exception e) {
            sendErrorResponse(response, "Token validation failed", JwtUtil.TokenErrorType.INVALID);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, String message, JwtUtil.TokenErrorType errorType) 
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        errorResponse.put("errorType", errorType.name());
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        
        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(errorResponse));
    }

    private String getRoleNameFromId(int roleId) {
        switch (roleId) {
            case 1:
                return "admin";
            case 2:
                return "user";
            default:
                return "unknown";
        }
    }
}
