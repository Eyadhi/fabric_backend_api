package com.example.fabric.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.fabric.dto.LoginRequest;
import com.example.fabric.dto.LoginResponseDto;
import com.example.fabric.model.ApiResponse;
import com.example.fabric.services.UserService;
import com.example.fabric.util.ResponseUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Authenticate a user and return a JWT token.
     * ResourceNotFoundException bubbles up if user not found (→ 404).
     * BadCredentialsException is caught by GlobalExceptionHandler (→ 401).
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody LoginRequest loginRequest) {
        LoginResponseDto responseDto = userService.login(loginRequest);
        return ResponseUtil.createSuccessResponse(responseDto);
    }
}
