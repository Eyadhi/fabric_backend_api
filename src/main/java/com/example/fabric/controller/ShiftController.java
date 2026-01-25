package com.example.fabric.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import com.example.fabric.model.User;
import com.example.fabric.services.ShiftService;
import com.example.fabric.services.UserService;
import com.example.fabric.util.ResponseUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ShiftController {
    
    private final ShiftService shiftService;
    private final UserService userService;
    
    /**
     * Create a new shift (Admin only)
     */
    @PostMapping("/admin/createshift")
    public ResponseEntity<?> createShift(@RequestBody AddShiftDto dto) {
        try {
            if (!isAdmin()) {
                return ResponseUtil.createErrorResponse(
                        HttpStatus.FORBIDDEN.value(),
                        "Access denied. Only admins can create shifts.");
            }

            Shift shift = shiftService.createShifts(dto);
            return ResponseUtil.createSuccessResponse(shift);
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse(500, "Error creating shift: " + e.getMessage());
        }
    }

    /**
     * Get all available shifts (Both admin and user can access)
     */
    @GetMapping("/all/shifts")
    public ResponseEntity<?> getAllShifts(
            @RequestParam(value = "id", required = false) Long id) {
        try {
            if (id != null) {
                Shift shift = shiftService.getShiftById(id);
                return ResponseUtil.createSuccessResponse(shift);
            }

            List<Shift> shifts = shiftService.getAllShifts();
            return ResponseUtil.createSuccessResponse(shifts);

        } catch (Exception e) {
            return ResponseUtil.createErrorResponse(
                    500,
                    "Error fetching shifts: " + e.getMessage()
            );
        }
    }

    /**
     * Get all available shifts (Admin access - for backward compatibility)
     */
    @GetMapping("/admin/shifts")
    public ResponseEntity<?> getShiftsAdmin(
            @RequestParam(value = "id", required = false) Long id) {
        try {
            if (id != null) {
                Shift shift = shiftService.getShiftById(id);
                return ResponseUtil.createSuccessResponse(shift);
            }

            List<Shift> shifts = shiftService.getAllShifts();
            return ResponseUtil.createSuccessResponse(shifts);

        } catch (Exception e) {
            return ResponseUtil.createErrorResponse(
                    500,
                    "Error fetching shifts: " + e.getMessage()
            );
        }
    }

    /**
     * Update an existing shift (Admin only)
     */
    @PutMapping("/admin/shifts/{id}")
    public ResponseEntity<?> updateShift(@PathVariable Long id, @RequestBody AddShiftDto dto) {
        try {
            if (!isAdmin()) {
                return ResponseUtil.createErrorResponse(
                        HttpStatus.FORBIDDEN.value(),
                        "Access denied. Only admins can update shifts.");
            }

            Shift shift = shiftService.updateShift(id, dto);
            return ResponseUtil.createSuccessResponse(shift);
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse(500, "Error updating shift: " + e.getMessage());
        }
    }

    /**
     * Delete a shift (Admin only)
     */
    @DeleteMapping("/admin/shifts/{id}")
    public ResponseEntity<?> deleteShift(@PathVariable Long id) {
        try {
            if (!isAdmin()) {
                return ResponseUtil.createErrorResponse(
                        HttpStatus.FORBIDDEN.value(),
                        "Access denied. Only admins can delete shifts.");
            }

            shiftService.deleteShift(id);
            return ResponseUtil.createSuccessResponse("Shift deleted successfully");
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse(500, "Error deleting shift: " + e.getMessage());
        }
    }

    private boolean isAdmin() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getName() != null) {
                String username = authentication.getName();
                User user = userService.findByUsername(username);
                return user != null && user.getRoleId() == 1; // 1 = admin
            }
        } catch (Exception e) {
            // Log error but don't expose details
        }
        return false;
    }
}