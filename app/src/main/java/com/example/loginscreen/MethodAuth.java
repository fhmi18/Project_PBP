package com.example.loginscreen;

import java.util.Objects;

public class MethodAuth {
    String confirmPassword, username, password;
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
    public void setConfirmPasswordPassword(String password) {
        this.confirmPassword = password;
    }
    public MethodAuth(String confirmPassword, String username, String password) {
            if (Objects.equals(confirmPassword, password)){
                this.username = username;
                this.password = password;
            }
    }

    public MethodAuth() {
        }
    }
