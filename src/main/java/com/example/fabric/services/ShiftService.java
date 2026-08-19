package com.example.fabric.services;

import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.fabric.dto.AddShiftDto;
import com.example.fabric.exceptions.BadRequestException;
import com.example.fabric.exceptions.BusinessException;
import com.example.fabric.exceptions.DuplicateResourceException;
import com.example.fabric.exceptions.ResourceNotFoundException;
import com.example.fabric.model.Shift;
import com.example.fabric.repository.ShiftRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShiftService {

    private final ShiftRepository shiftRepository;
    
    public List<Shift> getAllShifts() {
        return shiftRepository.findAll();
    }

    public Shift getShiftById(Long id) {
        return shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift", id));
    }

    public Shift updateShift(Long id, AddShiftDto dto) {
        Shift existingShift = getShiftById(id);
        
        // Parse time strings to LocalTime
        LocalTime startTime = LocalTime.parse(dto.getStartTime());
        LocalTime endTime = LocalTime.parse(dto.getEndTime());

        // Validate time - Allow night shifts that cross midnight
        boolean isNightShift = startTime.isAfter(endTime);
        // Skip time validation for night shifts that cross midnight
        if (!isNightShift && startTime.equals(endTime)) {
            throw new BadRequestException("Start time and end time cannot be the same");
        }

        // Check if shift name already exists (exclude current shift)
        Shift existingByName = shiftRepository.findByShiftName(dto.getShiftName());
        if (existingByName != null && !existingByName.getId().equals(id)) {
            throw new DuplicateResourceException(
                    "Shift name '" + dto.getShiftName() + "' already exists. Please use a different name.");
        }

        // Determine shift type
        int shiftTypeInt = resolveShiftType(dto);

        // Check if shift type already exists (exclude current shift)
        if (existingShift.getShiftType() != shiftTypeInt && shiftRepository.existsByShiftType(shiftTypeInt)) {
            throw new DuplicateResourceException(
                    "Shift type " + shiftTypeInt + " already exists. Each shift type must be unique.");
        }

        // Check overlapping shifts (exclude current shift) - skip for night shifts
        if (!isNightShift) {
            List<Shift> overlappingShifts = shiftRepository.findOverlappingShifts(startTime, endTime);
            overlappingShifts.removeIf(shift -> shift.getId().equals(id));
            if (!overlappingShifts.isEmpty()) {
                throw new BusinessException(
                        "Shift time conflicts with an existing shift between "
                                + dto.getStartTime() + " and " + dto.getEndTime());
            }
        }

        // Update shift
        existingShift.setShiftName(dto.getShiftName());
        existingShift.setShiftType(shiftTypeInt);
        existingShift.setStartTime(startTime);
        existingShift.setEndTime(endTime);
        existingShift.setDescription(dto.getDescription());

        return shiftRepository.save(existingShift);
    }

    public void deleteShift(Long id) {
        Shift shift = getShiftById(id);
        shiftRepository.delete(shift);
    }

    public Shift createShifts(AddShiftDto dto) {
        // Parse time strings to LocalTime
        LocalTime startTime = LocalTime.parse(dto.getStartTime());
        LocalTime endTime = LocalTime.parse(dto.getEndTime());

        // 1️⃣ Validate time - Allow night shifts that cross midnight
        boolean isNightShift = startTime.isAfter(endTime);
        if (!isNightShift && startTime.equals(endTime)) {
            throw new BadRequestException("Start time and end time cannot be the same");
        }

        // Check if shift name already exists
        if (shiftRepository.findByShiftName(dto.getShiftName()) != null) {
            throw new DuplicateResourceException(
                    "Shift name '" + dto.getShiftName() + "' already exists. Please use a different name.");
        }

        // Determine shift type
        int shiftTypeInt = resolveShiftType(dto);

        // Check if shift type already exists
        if (shiftRepository.existsByShiftType(shiftTypeInt)) {
            Shift existingShift = shiftRepository.findByShiftType(shiftTypeInt);
            if (existingShift != null && !existingShift.getShiftName().equals(dto.getShiftName())) {
                throw new DuplicateResourceException("Shift type " + shiftTypeInt
                        + " already exists with name '" + existingShift.getShiftName()
                        + "'. Each shift type must be unique.");
            }
        }

        // Check overlapping shifts
        if (!isNightShift) {
            List<Shift> overlappingShifts = shiftRepository.findOverlappingShifts(startTime, endTime);
            if (!overlappingShifts.isEmpty()) {
                throw new BusinessException(
                        "Shift time conflicts with an existing shift between "
                                + dto.getStartTime() + " and " + dto.getEndTime());
            }
        }

        // 6️⃣ Create shift
        Shift shift = new Shift();
        shift.setShiftName(dto.getShiftName());
        shift.setShiftType(shiftTypeInt);
        shift.setStartTime(startTime);
        shift.setEndTime(endTime);
        shift.setDescription(dto.getDescription());

        return shiftRepository.save(shift);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /** Converts a DTO's shiftType string/number to an int (1=Morning, 2=Night, 3=Evening). */
    private int resolveShiftType(AddShiftDto dto) {
        if (dto.getShiftType() != null) {
            try {
                return Integer.parseInt(dto.getShiftType());
            } catch (NumberFormatException e) {
                String t = dto.getShiftType().toLowerCase();
                if (t.equals("morning")) return 1;
                if (t.equals("night"))   return 2;
            }
        }
        return dto.getShiftName().equalsIgnoreCase("Morning") ? 1 : 2;
    }
}
