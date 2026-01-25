package com.example.fabric.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.fabric.dto.LoginRequest;
import com.example.fabric.dto.LoginResponseDto;
import com.example.fabric.model.ApiResponse;
import com.example.fabric.model.User;
import com.example.fabric.repository.UserRepository;
import com.example.fabric.services.UserService;
import com.example.fabric.util.ResponseUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody LoginRequest loginRequest) {
        Optional<User> optionalUser = userRepository.findByUsername(loginRequest.getUsername());
        if (optionalUser.isEmpty()) {
            return ResponseUtil.createErrorResponse(HttpStatus.NOT_FOUND.value(), "User not found");
        }
        User user = optionalUser.get();
        try {
            LoginResponseDto responseDto = userService.login(user);
            return ResponseUtil.createSuccessResponse(responseDto);
        } catch (BadCredentialsException e) {
            return ResponseUtil.createErrorResponse(HttpStatus.UNAUTHORIZED.value(),
                    "Invalid username or password");
        } catch (Exception ex) {
            return ResponseUtil.createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Unexpected error: " + ex.getMessage());
        }
    }
}
