package com.example.fabric.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import lombok.RequiredArgsConstructor;

import com.example.fabric.dto.AddRoleDto;
import com.example.fabric.model.Role;
import com.example.fabric.model.User;
import com.example.fabric.services.RoleService;
import com.example.fabric.services.UserService;
import com.example.fabric.util.ResponseUtil;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;
    private final UserService userService;

    @GetMapping("/getrole")
    public ResponseEntity<?> getRole(@RequestParam(value = "id", required = false) Long id) {
        try {
            List<Role> roles = (id != null) ? roleService.getRoleById(id) : roleService.getAllRoles();
            return ResponseUtil.createSuccessResponse(roles);
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Error fetching roles: " + e.getMessage());
        }
    }

    @PostMapping("/roles")
    public ResponseEntity<?> saveRole(@RequestBody AddRoleDto addRoleDto) {
        try {
            // Check if user is admin
            if (!isAdmin()) {
                return ResponseUtil.createErrorResponse(
                        HttpStatus.FORBIDDEN.value(),
                        "Access denied. Only admins can create roles.");
            }

            Role savedRole = roleService.saveRole(addRoleDto);
            return ResponseUtil.createSuccessResponse(savedRole);

        } catch (IllegalArgumentException ex) {
            return ResponseUtil.createErrorResponse(
                    HttpStatus.CONFLICT.value(),
                    ex.getMessage());

        } catch (Exception ex) {
            return ResponseUtil.createErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Unexpected error: " + ex.getMessage());
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
