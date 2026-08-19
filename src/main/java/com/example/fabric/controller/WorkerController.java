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
import com.example.fabric.projection.WorkerListView;
import com.example.fabric.services.WorkerService;
import com.example.fabric.util.ResponseUtil;

import lombok.RequiredArgsConstructor;

@RequestMapping("/users")
@RestController
@RequiredArgsConstructor
public class WorkerController {

    private final WorkerService workerService;

    @PostMapping("/addWorker")
    public ResponseEntity<?> saveWorker(@RequestBody AddWorkerDto dto) {
        // Duplicate-name check moved into WorkerService — throws DuplicateResourceException if taken
        Worker savedWorker = workerService.createWorker(dto);
        return new ResponseEntity<>(savedWorker, HttpStatus.CREATED);
    }

    @GetMapping("/getWorker")
    public ResponseEntity<?> getWorker(@RequestParam(value = "id", required = false) Long id) {
        List<WorkerListView> workers = (id != null)
                ? workerService.getWorkerById(id)
                : workerService.getAllWorkers();
        return ResponseUtil.createSuccessResponse(workers);
    }

    @PostMapping("/updateWorker")
    public ResponseEntity<?> updateWorker(@RequestBody UpdateWorkerDto dto) {
        Worker updatedWorker = workerService.updateWorker(dto);
        return ResponseUtil.createSuccessResponse(updatedWorker);
    }

    @GetMapping("/getWorkerAnalytics")
    public ResponseEntity<?> getWorkerAnalytics(
            @RequestParam Long workerId,
            @RequestParam String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Object analytics = workerService.getWorkerAnalytics(workerId, period, startDate, endDate);
        return ResponseUtil.createSuccessResponse(analytics);
    }
}
