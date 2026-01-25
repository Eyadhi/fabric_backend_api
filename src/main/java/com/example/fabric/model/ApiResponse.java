package com.example.fabric.model;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private boolean success;
    private int status;
    private T data;

    public ApiResponse(boolean success, int status, T data) {
        this.success = success;
        this.status = status;
        this.data = data;
    }
}
