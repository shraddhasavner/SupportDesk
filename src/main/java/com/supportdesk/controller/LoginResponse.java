package com.supportdesk.controller;

import com.supportdesk.entity.Role;

/**
 * LoginResponse: Response payload returned upon successful authentication.
 * 
 * [English]:
 * Contains signed JWT and basic user identity (id, name, email, role).
 * Never contains passwords or password hashes.
 * 
 * [Hinglish]:
 * Login successful hone par return hone wala response. Isme JWT token aur user info hoti hai.
 */
public class LoginResponse {

    private String token;
    private String type = "Bearer";
    private Long id;
    private String name;
    private String email;
    private Role role;

    public LoginResponse() {
    }

    public LoginResponse(String token, String type, Long id, String name, String email, Role role) {
        this.token = token;
        this.type = type != null ? type : "Bearer";
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
