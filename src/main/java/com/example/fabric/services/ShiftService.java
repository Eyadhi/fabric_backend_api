package com.example.fabric.services;

import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.fabric.dto.AddShiftDto;
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
                .orElseThrow(() -> new RuntimeException("Shift not found with ID: " + id));
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
            throw new IllegalArgumentException("Start time and end time cannot be the same");
        }

        // Check if shift name already exists (exclude current shift)
        Shift existingByName = shiftRepository.findByShiftName(dto.getShiftName());
        if (existingByName != null && !existingByName.getId().equals(id)) {
            throw new RuntimeException("Shift name '" + dto.getShiftName() + "' already exists. Please use a different name.");
        }

        // Determine shift type
        int shiftTypeInt;
        if (dto.getShiftType() != null) {
            try {
                shiftTypeInt = Integer.parseInt(dto.getShiftType());
            } catch (NumberFormatException e) {
                shiftTypeInt = dto.getShiftType().equalsIgnoreCase("Morning") ? 1 : 
                              dto.getShiftType().equalsIgnoreCase("Night") ? 2 : 
                              dto.getShiftType().equalsIgnoreCase("Evening") ? 3 : 1;
            }
        } else {
            shiftTypeInt = dto.getShiftName().equalsIgnoreCase("Morning") ? 1 : 2;
        }

        // Check if shift type already exists (exclude current shift)
        if (existingShift.getShiftType() != shiftTypeInt && shiftRepository.existsByShiftType(shiftTypeInt)) {
            throw new RuntimeException("Shift type " + shiftTypeInt + " already exists. Each shift type must be unique.");
        }

        // Check overlapping shifts (exclude current shift) - skip for night shifts
        if (!isNightShift) {
            List<Shift> overlappingShifts = shiftRepository.findOverlappingShifts(startTime, endTime);
            overlappingShifts.removeIf(shift -> shift.getId().equals(id));

            if (!overlappingShifts.isEmpty()) {
                throw new RuntimeException(
                        "Shift already exists between " +
                        dto.getStartTime() + " and " + dto.getEndTime()
                );
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
        // Skip time validation for night shifts that cross midnight
        if (!isNightShift && startTime.equals(endTime)) {
            throw new IllegalArgumentException("Start time and end time cannot be the same");
        }

        // 2️⃣ Check if shift name already exists
        if (shiftRepository.findByShiftName(dto.getShiftName()) != null) {
            throw new RuntimeException("Shift name '" + dto.getShiftName() + "' already exists. Please use a different name.");
        }

        // 3️⃣ Determine shift type
        int shiftTypeInt;
        if (dto.getShiftType() != null) {
            try {
                shiftTypeInt = Integer.parseInt(dto.getShiftType());
            } catch (NumberFormatException e) {
                shiftTypeInt = dto.getShiftType().equalsIgnoreCase("Morning") ? 1 : 
                              dto.getShiftType().equalsIgnoreCase("Night") ? 2 : 
                              dto.getShiftType().equalsIgnoreCase("Evening") ? 3 : 1;
            }
        } else {
            shiftTypeInt = dto.getShiftName().equalsIgnoreCase("Morning") ? 1 : 2;
        }

        // 4️⃣ Check if shift type already exists - Allow updates to existing types
        if (shiftRepository.existsByShiftType(shiftTypeInt)) {
            // Check if this is an update to the same shift type
            Shift existingShift = shiftRepository.findByShiftType(shiftTypeInt);
            if (existingShift != null && !existingShift.getShiftName().equals(dto.getShiftName())) {
                throw new RuntimeException("Shift type " + shiftTypeInt + " already exists with name '" + existingShift.getShiftName() + "'. Each shift type must be unique.");
            }
        }

        // 5️⃣ Check overlapping shifts (skip for now as night shifts are complex to validate)
        // For night shifts crossing midnight, overlap validation is more complex
        if (!isNightShift) {
            List<Shift> overlappingShifts = shiftRepository.findOverlappingShifts(startTime, endTime);
            if (!overlappingShifts.isEmpty()) {
                throw new RuntimeException(
                        "Shift already exists between " +
                        dto.getStartTime() + " and " + dto.getEndTime()
                );
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

}
