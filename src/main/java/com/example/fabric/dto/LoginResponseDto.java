package com.example.fabric.dto;

import java.time.LocalDateTime;

public record LoginResponseDto(String username, String role, String token, LocalDateTime tokenExpiry) {
}