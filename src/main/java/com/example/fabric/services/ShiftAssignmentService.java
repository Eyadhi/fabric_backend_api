package com.example.fabric.services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.fabric.exceptions.BusinessException;
import com.example.fabric.exceptions.ResourceNotFoundException;
import com.example.fabric.dto.ShiftAssignmentDto;
import com.example.fabric.model.Machine;
import com.example.fabric.model.Shift;
import com.example.fabric.model.Worker;
import com.example.fabric.model.WorkerShiftAssignment;
import com.example.fabric.repository.MachineRepository;
import com.example.fabric.repository.ShiftAssignmentRepository;
import com.example.fabric.repository.ShiftRepository;
import com.example.fabric.repository.WorkerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ShiftAssignmentService {

    private final ShiftAssignmentRepository assignmentRepository;
    private final WorkerRepository workerRepository;
    private final MachineRepository machineRepository;
    private final ShiftRepository shiftRepository;

    /**
     * Create a flexible shift assignment (weekly or custom range)
     */
    public WorkerShiftAssignment createFlexibleAssignment(ShiftAssignmentDto dto) {
        // Validate entities
        Worker worker = workerRepository.findById(dto.getWorkerId())
                .orElseThrow(() -> new ResourceNotFoundException("Worker", dto.getWorkerId()));

        Machine machine = machineRepository.findById(dto.getMachineId())
                .orElseThrow(() -> new ResourceNotFoundException("Machine", dto.getMachineId()));

        Shift shift = shiftRepository.findById(dto.getShiftId())
                .orElseThrow(() -> new ResourceNotFoundException("Shift", dto.getShiftId()));

        // Calculate dates based on assignment type
        LocalDate periodStart = dto.getPeriodStartDate();
        LocalDate periodEnd = dto.getPeriodEndDate();

        if (dto.getAssignmentType() == 1) {
            // For weekly assignments, ensure period starts on Saturday
            periodStart = getSaturday(periodStart);
            periodEnd = periodStart.plusDays(6);
            ;
        }

        // Check for overlapping assignments
        if (hasOverlappingAssignment(dto.getWorkerId(), dto.getMachineId(), periodStart, periodEnd)) {
            throw new BusinessException(
                    "Worker already has an assignment for this machine in the specified period");
        }

        // Create assignment
        WorkerShiftAssignment assignment = new WorkerShiftAssignment();
        assignment.setWorker(worker);
        assignment.setMachine(machine);
        assignment.setShift(shift);
        assignment.setAssignmentType(dto.getAssignmentType());
        assignment.setPeriodStartDate(periodStart);
        assignment.setPeriodEndDate(periodEnd);
        assignment.setIsActive(true);
        return assignmentRepository.save(assignment);
    }

    /**
     * Get Saturday of the week containing the given date
     */
    private LocalDate getSaturday(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        int daysToSaturday = DayOfWeek.SATURDAY.getValue() - dayOfWeek.getValue();
        if (daysToSaturday > 0) {
            daysToSaturday -= 7; // Go to previous Saturday
        }
        return date.plusDays(daysToSaturday);
    }

    /**
     * Check for overlapping assignments
     */
    private boolean hasOverlappingAssignment(Long workerId, Long machineId, LocalDate periodStartDate,
            LocalDate periodEndDate) {
        List<WorkerShiftAssignment> overlapping = assignmentRepository.findByWorkerIdAndIsActive(workerId, true)
                .stream()
                .filter(assignment -> assignment.getMachine().getId().equals(machineId))
                .filter(assignment -> !(periodEndDate.isBefore(assignment.getPeriodStartDate()) ||
                        periodStartDate.isAfter(assignment.getPeriodEndDate())))
                .collect(Collectors.toList());

        return !overlapping.isEmpty();
    }

    /**
     * Get assignments for salary calculation
     */
    public List<ShiftAssignmentDto> getAssignmentsForSalaryCalculation(LocalDate endDate, Integer weeks) {
        LocalDate startDate = endDate.minusDays((weeks * 7) - 1);

        List<WorkerShiftAssignment> assignments = assignmentRepository.findAll()
                .stream()
                .filter(assignment -> assignment.getIsActive())
                .filter(assignment -> !(endDate.isBefore(assignment.getPeriodStartDate()) ||
                        startDate.isAfter(assignment.getPeriodEndDate())))
                .collect(Collectors.toList());

        return assignments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Convert WorkerShiftAssignment to DTO
     */
    private ShiftAssignmentDto convertToDto(WorkerShiftAssignment assignment) {
        ShiftAssignmentDto dto = new ShiftAssignmentDto();
        dto.setWorkerId(assignment.getWorker().getId());
        dto.setMachineId(assignment.getMachine().getId());
        dto.setShiftId(assignment.getShift().getId());
        dto.setPeriodStartDate(assignment.getPeriodStartDate());
        dto.setPeriodEndDate(assignment.getPeriodEndDate());
        dto.setWorkerName(assignment.getWorker().getWorkerName());
        dto.setMachineCode(assignment.getMachine().getMachineCode());
        dto.setShiftName(assignment.getShift().getShiftName());
        dto.setIsActive(assignment.getIsActive());
        dto.setAssignmentType(assignment.getAssignmentType());
        return dto;
    }

    /**
     * Create weekly assignment (Saturday to Friday)
     */
    public WorkerShiftAssignment createWeeklyAssignment(Long workerId, Long machineId, Long shiftId,
            LocalDate weekStartDate, Integer calculationWeeks) {
        ShiftAssignmentDto dto = new ShiftAssignmentDto();
        dto.setWorkerId(workerId);
        dto.setMachineId(machineId);
        dto.setShiftId(shiftId);
        dto.setPeriodStartDate(weekStartDate);
        dto.setPeriodEndDate(weekStartDate.plusDays(6)); // Saturday to Friday
        dto.setAssignmentType(1); // Weekly type

        return createFlexibleAssignment(dto);
    }

    /**
     * Create custom range assignment
     */
    public WorkerShiftAssignment createCustomRangeAssignment(Long workerId, Long machineId, Long shiftId,
            LocalDate startDate, LocalDate endDate, Integer calculationWeeks) {
        ShiftAssignmentDto dto = new ShiftAssignmentDto();
        dto.setWorkerId(workerId);
        dto.setMachineId(machineId);
        dto.setShiftId(shiftId);
        dto.setPeriodStartDate(startDate);
        dto.setPeriodEndDate(endDate);
        dto.setAssignmentType(2); // Custom range type

        return createFlexibleAssignment(dto);
    }

    /**
     * Get worker assignments for a period
     */
    public List<ShiftAssignmentDto> getWorkerAssignments(Long workerId, LocalDate startDate, LocalDate endDate) {
        List<WorkerShiftAssignment> assignments = assignmentRepository.findByWorkerIdAndIsActive(workerId, true)
                .stream()
                .filter(assignment -> !(endDate.isBefore(assignment.getPeriodStartDate()) ||
                        startDate.isAfter(assignment.getPeriodEndDate())))
                .collect(Collectors.toList());

        return assignments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<ShiftAssignmentDto> getAllWorkerAssignments(LocalDate startDate, LocalDate endDate) {
        List<WorkerShiftAssignment> assignments = assignmentRepository.findByIsActive(true)
                .stream()
                .filter(assignment -> !(endDate.isBefore(assignment.getPeriodStartDate()) ||
                        startDate.isAfter(assignment.getPeriodEndDate())))
                .collect(Collectors.toList());

        return assignments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}