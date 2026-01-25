package com.example.fabric.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.fabric.dto.AddWorkerDto;
import com.example.fabric.dto.UpdateWorkerDto;
import com.example.fabric.model.Worker;
import com.example.fabric.repository.WorkerRepository;
import com.example.fabric.services.WorkerService;
import com.example.fabric.util.ResponseUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RequestMapping("/users")
@RestController
@RequiredArgsConstructor
public class WorkerController {

    private final WorkerService workerService;
    private final WorkerRepository workerRepository;

    @PostMapping("/addWorker")
    public ResponseEntity<?> saveWoker(@RequestBody AddWorkerDto dto, HttpServletRequest httpRequest) {
        List<Worker> existingUser = workerRepository.findByWorkerName(dto.getName());

        if (existingUser != null && !existingUser.isEmpty()) {
            return ResponseUtil.createErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Username is already taken");
        }

        try {
            Worker savedWorker = workerService.createWorker(dto);
            return new ResponseEntity<>(savedWorker, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error adding role: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getWorker")
    public List<Worker> getWorker(@RequestParam(value = "id", required = false) Long id) {
        return (id != null) ? workerService.getWorkerById(id)
                : workerService.getAllWorkers();
    }

    @PostMapping("/updateWorker")
    public ResponseEntity<?> updateWorker(@RequestBody UpdateWorkerDto dto) {
        try {
            Worker updatedWorker = workerService.updateWorker(dto);
            return ResponseUtil.createSuccessResponse(updatedWorker);
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Error updating worker: " + e.getMessage());
        }
    }

    @GetMapping("/getWorkerAnalytics")
    public ResponseEntity<?> getWorkerAnalytics(@RequestParam Long workerId, 
                                               @RequestParam String period,
                                               @RequestParam(required = false) String startDate,
                                               @RequestParam(required = false) String endDate) {
        try {
            Object analytics = workerService.getWorkerAnalytics(workerId, period, startDate, endDate);
            return ResponseUtil.createSuccessResponse(analytics);
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Error fetching worker analytics: " + e.getMessage());
        }
    }

}
