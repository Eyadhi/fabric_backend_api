package com.example.fabric.enums;

public enum RoleEnum {
    ADMIN(1, "ADMIN", "Administrator with full access"),
    USER(2, "USER", "Regular user with limited access"),
    MANAGER(3, "MANAGER", "Manager with intermediate access");

    private final int id;
    private final String name;
    private final String description;

    RoleEnum(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public static RoleEnum fromId(int id) {
        for (RoleEnum role : RoleEnum.values()) {
            if (role.id == id) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid role ID: " + id);
    }

    public static RoleEnum fromName(String name) {
        for (RoleEnum role : RoleEnum.values()) {
            if (role.name.equalsIgnoreCase(name)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid role name: " + name);
    }
}
