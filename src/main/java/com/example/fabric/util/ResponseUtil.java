package com.example.fabric.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.fabric.model.ApiResponse;

public class ResponseUtil {

    public static <T> ResponseEntity<ApiResponse<?>> createSuccessResponse(T data) {
        return new ResponseEntity<>(new ApiResponse<>(true, HttpStatus.OK.value(), data), HttpStatus.OK);
    }

    public static ResponseEntity<ApiResponse<?>> createErrorResponse(int status, String message) {
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("error", message);
        ApiResponse<Map<String, Object>> response = new ApiResponse<>(false, status, errorData);

        return new ResponseEntity<>(response, HttpStatus.valueOf(status));
    }
}
