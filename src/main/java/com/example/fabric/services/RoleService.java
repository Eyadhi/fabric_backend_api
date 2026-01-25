package com.example.fabric.services;

import com.example.fabric.dto.AddRoleDto;
import com.example.fabric.model.Role;
import com.example.fabric.repository.RoleRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public Role saveRole(AddRoleDto dto) {
        Role existingRole = roleRepository.findByRoleName(dto.getRoleName());

        if (existingRole != null) {
            throw new RuntimeException("Role already exists: " + dto.getRoleName());
        }
        Role role = new Role();
        role.setRoleName(dto.getRoleName());
        return roleRepository.save(role);
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public List<Role> getRoleById(Long id) {
        return roleRepository.findById(id).map(Collections::singletonList).orElse(Collections.emptyList());
    }

}
