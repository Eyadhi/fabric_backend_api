package com.example.fabric.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.fabric.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByRoleName(String roleName);
}