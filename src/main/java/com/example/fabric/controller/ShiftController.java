package com.example.fabric.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.fabric.dto.AddShiftDto;
import com.example.fabric.model.Shift;
import com.example.fabric.services.ShiftService;
import com.example.fabric.util.ResponseUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService shiftService;

    /**
     * Create a new shift — admin only.
     * @PreAuthorize replaces every manual isAdmin() check.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/createshift")
    public ResponseEntity<?> createShift(@RequestBody AddShiftDto dto) {
        Shift shift = shiftService.createShifts(dto);
        return ResponseUtil.createSuccessResponse(shift);
    }

    @GetMapping("/all/shifts")
    public ResponseEntity<?> getAllShifts(@RequestParam(value = "id", required = false) Long id) {
        if (id != null) {
            return ResponseUtil.createSuccessResponse(shiftService.getShiftById(id));
        }
        List<Shift> shifts = shiftService.getAllShifts();
        return ResponseUtil.createSuccessResponse(shifts);
    }

    /** Kept for backward compatibility — delegates to the same service. */
    @GetMapping("/admin/shifts")
    public ResponseEntity<?> getShiftsAdmin(@RequestParam(value = "id", required = false) Long id) {
        return getAllShifts(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/shifts/{id}")
    public ResponseEntity<?> updateShift(@PathVariable Long id, @RequestBody AddShiftDto dto) {
        Shift shift = shiftService.updateShift(id, dto);
        return ResponseUtil.createSuccessResponse(shift);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/shifts/{id}")
    public ResponseEntity<?> deleteShift(@PathVariable Long id) {
        shiftService.deleteShift(id);
        return ResponseUtil.createSuccessResponse("Shift deleted successfully");
    }
}
