package com.example.fabric.services;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.fabric.dto.AddWorkerDto;
import com.example.fabric.dto.UpdateWorkerDto;
import com.example.fabric.model.Worker;
import com.example.fabric.repository.WorkerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkerService {
    private final WorkerRepository workerRepository;
    private final MeterService meterService;

    public Worker createWorker(AddWorkerDto dto) {

        Worker newWorker = new Worker();
        newWorker.setWorkerName(dto.getName());
        newWorker.setMobile(dto.getMobile());

        Worker savedWorker = workerRepository.save(newWorker);
        return savedWorker;

    }

    public List<Worker> getAllWorkers() {
        return workerRepository.findAll();
    }

    public List<Worker> getWorkerById(Long id) {
        return workerRepository.findById(id).map(Collections::singletonList).orElse(Collections.emptyList());
    }

    public Worker updateWorker(UpdateWorkerDto dto) {
        Worker worker = workerRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Worker not found with id: " + dto.getId()));
        
        // Check if worker code is being changed and if it's unique
        if (dto.getWorkerCode() != null && !dto.getWorkerCode().equals(worker.getWorkerCode())) {
            Worker existingWorkerWithCode = workerRepository.findByWorkerCode(dto.getWorkerCode());
            if (existingWorkerWithCode != null && !existingWorkerWithCode.getId().equals(dto.getId())) {
                throw new RuntimeException("Worker code already exists: " + dto.getWorkerCode());
            }
            worker.setWorkerCode(dto.getWorkerCode());
        }
        
        if (dto.getWorkerName() != null) {
            worker.setWorkerName(dto.getWorkerName());
        }
        
        if (dto.getMobile() != null) {
            worker.setMobile(dto.getMobile());
        }
        
        return workerRepository.save(worker);
    }

    public Object getWorkerAnalytics(Long workerId, String period, String startDate, String endDate) {
        // Validate worker exists
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("Worker not found with id: " + workerId));
        
        java.time.LocalDate start = null;
        java.time.LocalDate end = null;
        
        // Calculate date range based on period
        java.time.LocalDate now = java.time.LocalDate.now();
        
        switch (period.toLowerCase()) {
            case "weekly":
                // Use provided dates if available (for week navigation), otherwise calculate current week
                if (startDate != null && endDate != null) {
                    start = java.time.LocalDate.parse(startDate);
                    end = java.time.LocalDate.parse(endDate);
                } else {
                    // Calculate Saturday to Friday week
                    java.time.DayOfWeek currentDay = now.getDayOfWeek();
                    int daysFromSaturday = (currentDay.getValue() + 1) % 7; // Saturday = 0, Sunday = 1, etc.
                    start = now.minusDays(daysFromSaturday).minusWeeks(0); // Current week's Saturday
                    end = start.plusDays(6); // Friday of the same week
                }
                break;
            case "monthly":
                // For monthly, we'll use the provided startDate and endDate which will contain month/year info
                if (startDate != null && endDate != null) {
                    start = java.time.LocalDate.parse(startDate);
                    end = java.time.LocalDate.parse(endDate);
                } else {
                    // Default to current month if no specific month/year provided
                    start = now.withDayOfMonth(1);
                    end = now.withDayOfMonth(now.lengthOfMonth());
                }
                break;
            case "yearly":
                start = now.minusYears(1);
                end = now;
                break;
            case "custom":
                if (startDate != null && endDate != null) {
                    start = java.time.LocalDate.parse(startDate);
                    end = java.time.LocalDate.parse(endDate);
                } else {
                    throw new RuntimeException("Start date and end date are required for custom period");
                }
                break;
            default:
                throw new RuntimeException("Invalid period. Use: weekly, monthly, yearly, or custom");
        }
        
        // Get analytics data using MeterService with both start and end dates
        java.util.Map<String, Object> analytics = meterService.getTotalMetersAndCost(workerId, start, end);
        
        // Add worker information and period details
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("worker", worker);
        result.put("period", period);
        result.put("startDate", start.toString());
        result.put("endDate", end.toString());
        result.put("analytics", analytics);
        
        return result;
    }
}
