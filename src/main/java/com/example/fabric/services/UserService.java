package com.example.fabric.services;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.fabric.dto.LoginRequest;
import com.example.fabric.dto.LoginResponseDto;
import com.example.fabric.dto.RegisterDto;
import com.example.fabric.exceptions.BadRequestException;
import com.example.fabric.exceptions.DuplicateResourceException;
import com.example.fabric.exceptions.ResourceNotFoundException;
import com.example.fabric.model.User;
import com.example.fabric.repository.UserRepository;
import com.example.fabric.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Register a new application user.
     * Called by AdminController — duplicate-username check lives here, not in the controller.
     */
    public void register(RegisterDto dto) {
        if (userRepository.findByUsername(dto.username()).isPresent()) {
            throw new DuplicateResourceException("Username is already taken: " + dto.username());
        }

        User user = new User();
        user.setUsername(dto.username());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setMobileNo(dto.mobile());
        user.setRoleId(dto.roleId() != null ? dto.roleId() : 2); // default to USER role
        userRepository.save(user);
    }

    /**
     * Authenticate with username + password and return a JWT response.
     * Called by UserController — credential validation lives here, not in the controller.
     */
    public LoginResponseDto login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.username()));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadRequestException("Invalid username or password");
        }

        return generateTokenResponse(user);
    }

    /** Internal use — called by JwtAuthFilter / other services that already have the User entity. */
    public LoginResponseDto login(User user) {
        return generateTokenResponse(user);
    }

    public User findByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.orElse(null);
    }

    public User createUser(RegisterDto dto, String defaultPassword) {
        User newUser = new User();
        newUser.setUsername(dto.username());
        newUser.setPassword(passwordEncoder.encode(dto.password()));
        newUser.setMobileNo(dto.mobile());
        userRepository.save(newUser);
        return newUser;
    }

    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private LoginResponseDto generateTokenResponse(User user) {
        LocalDateTime now = LocalDateTime.now();
        String token;

        if (user.getToken() != null && user.getTokenExpiry() != null && user.getTokenExpiry().isAfter(now)) {
            token = user.getToken();
        } else {
            token = jwtUtil.generateToken(user.getUsername(), user.getRoleId());
            LocalDateTime expiry = jwtUtil.getExpirationDateFromToken(token)
                    .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            user.setToken(token);
            user.setTokenExpiry(expiry);
            userRepository.save(user);
        }

        return new LoginResponseDto(
                user.getUsername(),
                user.getRoleName(),
                token,
                user.getTokenExpiry());
    }
}