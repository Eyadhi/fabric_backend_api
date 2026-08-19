package com.example.fabric.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.fabric.dto.AddRoleDto;
import com.example.fabric.model.Role;
import com.example.fabric.services.RoleService;
import com.example.fabric.util.ResponseUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping("/getrole")
    public ResponseEntity<?> getRole(@RequestParam(value = "id", required = false) Long id) {
        List<Role> roles = (id != null) ? roleService.getRoleById(id) : roleService.getAllRoles();
        return ResponseUtil.createSuccessResponse(roles);
    }

    /**
     * @PreAuthorize replaces the manual isAdmin() check.
     * IllegalArgumentException (duplicate role) bubbles to GlobalExceptionHandler → 409.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/roles")
    public ResponseEntity<?> saveRole(@RequestBody AddRoleDto addRoleDto) {
        Role savedRole = roleService.saveRole(addRoleDto);
        return ResponseUtil.createSuccessResponse(savedRole);
    }
}
