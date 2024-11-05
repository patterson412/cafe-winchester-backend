package com.projects.cafe_winchester_backend.dto;

import jakarta.validation.constraints.NotNull;

public class LoginFormDto {
    @NotNull
    private String username;
    @NotNull
    private String password;

    public LoginFormDto() {
    }

    public LoginFormDto(String username, String password) {
        this.username = username;
        this.password = password;
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

    @Override
    public String toString() {
        return "LoginFormDto{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
