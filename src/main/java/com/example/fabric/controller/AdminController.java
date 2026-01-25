package com.example.fabric.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.fabric.dto.RegisterDto;
import com.example.fabric.model.ApiResponse;
import com.example.fabric.model.User;
import com.example.fabric.repository.UserRepository;
import com.example.fabric.services.UserService;
import com.example.fabric.util.ResponseUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(@RequestBody RegisterDto registerRequest) {
        try {
            // Check if user is admin
            if (!isAdmin()) {
                return ResponseUtil.createErrorResponse(
                        HttpStatus.FORBIDDEN.value(),
                        "Access denied. Only admins can register new users.");
            }

            Optional<User> existingUser = userRepository.findByUsername(registerRequest.getUsername());

            if (existingUser.isPresent()) {
                return ResponseUtil.createErrorResponse(HttpStatus.BAD_REQUEST.value(),
                        "Username is already taken");
            }

            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            user.setMobileNo(registerRequest.getMobile());
            user.setRoleId(registerRequest.getRoleId() != null ? registerRequest.getRoleId() : 2); // Default to user role

            userRepository.save(user);

            return ResponseUtil.createSuccessResponse("User registered successfully");
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Error registering user: " + e.getMessage());
        }
    }

    private boolean isAdmin() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getName() != null) {
                String username = authentication.getName();
                User user = userService.findByUsername(username);
                return user != null && user.getRoleId() == 1; // 1 = admin
            }
        } catch (Exception e) {
            // Log error but don't expose details
        }
        return false;
    }
}
