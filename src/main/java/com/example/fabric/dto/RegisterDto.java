package com.example.fabric.dto;

import lombok.Data;

@Data
public class RegisterDto {
    private String username;
    private String password;
    private String mobile;
    private Integer roleId; // 1 = admin, 2 = user
}
