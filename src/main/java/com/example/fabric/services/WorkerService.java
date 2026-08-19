package com.example.fabric.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.fabric.dto.AddWorkerDto;
import com.example.fabric.dto.UpdateWorkerDto;
import com.example.fabric.exceptions.DuplicateResourceException;
import com.example.fabric.exceptions.ResourceNotFoundException;
import com.example.fabric.model.Worker;
import com.example.fabric.projection.WorkerListView;
import com.example.fabric.repository.WorkerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkerService {
    private final WorkerRepository workerRepository;
    private final MeterService meterService;

    public Worker createWorker(AddWorkerDto dto) {
        List<Worker> existing = workerRepository.findByWorkerName(dto.getName());
        if (existing != null && !existing.isEmpty()) {
            throw new DuplicateResourceException("Worker name is already taken: " + dto.getName());
        }

        Worker newWorker = new Worker();
        newWorker.setWorkerName(dto.getName());
        newWorker.setMobile(dto.getMobile());
        return workerRepository.save(newWorker);
    }

    public List<WorkerListView> getAllWorkers() {
        return workerRepository.findAllWorkersView();
    }

    public List<WorkerListView> getWorkerById(Long id) {
        return workerRepository.findWorkerByIdView(id);
    }

    public Worker updateWorker(UpdateWorkerDto dto) {
        Worker worker = workerRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Worker", dto.getId()));

        // Check if worker code is being changed and if it's unique
        if (dto.getWorkerCode() != null && !dto.getWorkerCode().equals(worker.getWorkerCode())) {
            Worker existingWorkerWithCode = workerRepository.findByWorkerCode(dto.getWorkerCode());
            if (existingWorkerWithCode != null && !existingWorkerWithCode.getId().equals(dto.getId())) {
                throw new DuplicateResourceException("Worker code already exists: " + dto.getWorkerCode());
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
                .orElseThrow(() -> new ResourceNotFoundException("Worker", workerId));

        java.time.LocalDate start = null;
        java.time.LocalDate end = null;

        java.time.LocalDate now = java.time.LocalDate.now();

        switch (period.toLowerCase()) {
            case "weekly":
                if (startDate != null && endDate != null) {
                    start = java.time.LocalDate.parse(startDate);
                    end = java.time.LocalDate.parse(endDate);
                } else {
                    java.time.DayOfWeek currentDay = now.getDayOfWeek();
                    int daysFromSaturday = (currentDay.getValue() + 1) % 7;
                    start = now.minusDays(daysFromSaturday);
                    end = start.plusDays(6);
                }
                break;
            case "monthly":
                if (startDate != null && endDate != null) {
                    start = java.time.LocalDate.parse(startDate);
                    end = java.time.LocalDate.parse(endDate);
                } else {
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
                    throw new com.example.fabric.exceptions.BadRequestException(
                            "startDate and endDate are required for custom period");
                }
                break;
            default:
                throw new com.example.fabric.exceptions.BadRequestException(
                        "Invalid period '" + period + "'. Use: weekly, monthly, yearly, or custom");
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
