package com.smartbooking.domain;

public class User {
    private final long id;
    private final String username;
    private final String passwordHash;
    private final Role role;

    public User(long id, String username, String passwordHash, Role role) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }
}
