package com.soumyajit.apigateway.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Document(collection = "users")
public class User {
    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    private String password;

    private String displayName;

    private Instant createdAt;

    // THE FIX: Initialize here to prevent NullPointerException on .stream()
    private Set<String> roles = new HashSet<>();

    // Standard no-args constructor required by MongoDB
    public User() {
    }

    public User(String id, String username, String password, String displayName, Instant createdAt, Set<String> roles) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.displayName = displayName;
        this.createdAt = createdAt;
        this.roles = roles != null ? roles : new HashSet<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}