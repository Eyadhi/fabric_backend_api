package com.example.fabric.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.fabric.dto.MeterRequest;
import com.example.fabric.model.Meter;
import com.example.fabric.services.MeterService;
import com.example.fabric.util.ResponseUtil;

import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;


@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class MeterController {
    private final MeterService meterService;

    @PostMapping("/savemeter")
    public ResponseEntity<?> saveMeter(@Valid @RequestBody MeterRequest request) {
        try {
            Meter savedMeter = meterService.saveMeterProduction(request);
            return ResponseUtil.createSuccessResponse(savedMeter);
        } catch (Exception ex) {
            return ResponseUtil.createErrorResponse(500, "Error saving meter: " + ex.getMessage());
        }
    }
}
