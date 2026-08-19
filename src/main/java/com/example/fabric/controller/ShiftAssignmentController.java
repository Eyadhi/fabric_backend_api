package com.example.fabric.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.fabric.dto.ShiftAssignmentDto;
import com.example.fabric.model.WorkerShiftAssignment;
import com.example.fabric.services.ShiftAssignmentService;
import com.example.fabric.util.ResponseUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users/shifts")
@RequiredArgsConstructor
public class ShiftAssignmentController {

    private final ShiftAssignmentService shiftAssignmentService;

    @PostMapping("/assign")
    public ResponseEntity<?> createFlexibleAssignment(@RequestBody ShiftAssignmentDto dto) {
        WorkerShiftAssignment assignment = shiftAssignmentService.createFlexibleAssignment(dto);
        return ResponseUtil.createSuccessResponse(assignment);
    }

    @PostMapping("/assign-weekly")
    public ResponseEntity<?> createWeeklyAssignment(
            @RequestParam Long workerId,
            @RequestParam Long machineId,
            @RequestParam Long shiftId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate,
            @RequestParam(defaultValue = "1") Integer calculationWeeks) {

        WorkerShiftAssignment assignment = shiftAssignmentService
                .createWeeklyAssignment(workerId, machineId, shiftId, weekStartDate, calculationWeeks);
        return ResponseUtil.createSuccessResponse(assignment);
    }

    @PostMapping("/assign-custom")
    public ResponseEntity<?> createCustomRangeAssignment(
            @RequestParam Long workerId,
            @RequestParam Long machineId,
            @RequestParam Long shiftId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") Integer calculationWeeks) {

        WorkerShiftAssignment assignment = shiftAssignmentService
                .createCustomRangeAssignment(workerId, machineId, shiftId, startDate, endDate, calculationWeeks);
        return ResponseUtil.createSuccessResponse(assignment);
    }

    @GetMapping("/worker-assignments")
    public ResponseEntity<?> getWorkerAssignments(
            @RequestParam Long workerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<ShiftAssignmentDto> assignments =
                shiftAssignmentService.getWorkerAssignments(workerId, startDate, endDate);
        return ResponseUtil.createSuccessResponse(assignments);
    }

    @GetMapping("/all-worker-assignments")
    public ResponseEntity<?> getAllWorkerAssignments(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<ShiftAssignmentDto> assignments =
                shiftAssignmentService.getAllWorkerAssignments(startDate, endDate);
        return ResponseUtil.createSuccessResponse(assignments);
    }
}
