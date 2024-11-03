package com.projects.cafe_winchester_backend.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

@Service
public class UserManagementService {
    private final JdbcUserDetailsManager userDetailsManager;
    private final PasswordEncoder passwordEncoder;

    public UserManagementService(UserDetailsManager userDetailsManager, PasswordEncoder passwordEncoder) {
        this.userDetailsManager = (JdbcUserDetailsManager) userDetailsManager;
        this.passwordEncoder = passwordEncoder;
    }

    public void createUser(String username, String password, String role) {
        UserDetails user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .roles(role)
                .disabled(false)
                .build();

        userDetailsManager.createUser(user);
    }

    public void updateUser(String username, String newPassword) {
        UserDetails existingUser = userDetailsManager.loadUserByUsername(username);
        UserDetails updatedUser = User.builder()
                .username(username)
                .password(passwordEncoder.encode(newPassword))
                .authorities(existingUser.getAuthorities())
                .disabled(!existingUser.isEnabled())
                .build();

        userDetailsManager.updateUser(updatedUser);
    }

    public void deleteUser(String username) {
        userDetailsManager.deleteUser(username);
    }

    public void changeUserRole(String username, String newRole) {
        UserDetails existingUser = userDetailsManager.loadUserByUsername(username);
        UserDetails updatedUser = User.builder()
                .username(existingUser.getUsername())
                .password(existingUser.getPassword())
                .roles(newRole)
                .disabled(!existingUser.isEnabled())
                .build();

        userDetailsManager.updateUser(updatedUser);
    }

    public void disableUser(String username) {
        UserDetails existingUser = userDetailsManager.loadUserByUsername(username);
        UserDetails updatedUser = User.builder()
                .username(username)
                .password(existingUser.getPassword())
                .authorities(existingUser.getAuthorities())
                .disabled(true)  // Sets active=false in database
                .build();

        userDetailsManager.updateUser(updatedUser);
    }

    public void enableUser(String username) {
        UserDetails existingUser = userDetailsManager.loadUserByUsername(username);
        UserDetails updatedUser = User.builder()
                .username(username)
                .password(existingUser.getPassword())
                .authorities(existingUser.getAuthorities())
                .disabled(false)  // Sets active=true in database
                .build();

        userDetailsManager.updateUser(updatedUser);
    }

    public boolean userExists(String username) {
        return userDetailsManager.userExists(username);
    }
}
