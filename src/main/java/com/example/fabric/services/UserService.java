package com.example.fabric.services;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.fabric.dto.LoginResponseDto;
import com.example.fabric.dto.RegisterDto;
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

    public User createUser(RegisterDto dto, String defaultPassword) {
        User newUser = new User();
        newUser.setUsername(dto.getUsername());
        newUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        newUser.setMobileNo(dto.getMobile());
        userRepository.save(newUser);

        return newUser;
    }

    public User findByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.orElse(null);
    }

    public LoginResponseDto login(User user) {
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
                token,
                user.getTokenExpiry());
    }

    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }
}