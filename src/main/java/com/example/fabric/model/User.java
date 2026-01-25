package com.example.fabric.model;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String password;

    @Column(name = "mobile_no")
    private String mobileNo;

    private String token;

    @Column(name = "token_expiry")
    private LocalDateTime tokenExpiry;

    private boolean status = true;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    private int roleId;

    @Transient
    public String getRoleName() {
        switch (this.roleId) {
            case 1:
                return "admin";
            case 2:
                return "user";
            default:
                return "unknown";
        }
    }
}
