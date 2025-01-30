package com.projects.cafe_winchester_backend.service;

import com.projects.cafe_winchester_backend.entity.*;
import com.projects.cafe_winchester_backend.repository.UserDao;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class UserService {
    private final UserDetailsManager userDetailsManager;
    private final PasswordEncoder passwordEncoder;
    private final UserDao userDao;

    public UserService(UserDetailsManager userDetailsManager,
                       PasswordEncoder passwordEncoder,
                       UserDao userDao) {
        this.userDetailsManager = userDetailsManager;
        this.passwordEncoder = passwordEncoder;
        this.userDao = userDao;
    }

    // Security Management Methods
    public void createUser(String username, String password, String[] roles) {
        UserDetails user = org.springframework.security.core.userdetails.User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .roles(roles)
                .disabled(false)
                .build();
        userDetailsManager.createUser(user);
    }

    public void updateUserCredentials(String username, String newPassword) {
        UserDetails existingUser = userDetailsManager.loadUserByUsername(username);
        UserDetails updatedUser = org.springframework.security.core.userdetails.User.builder()
                .username(username)
                .password(passwordEncoder.encode(newPassword))
                .authorities(existingUser.getAuthorities())
                .disabled(!existingUser.isEnabled())
                .build();
        userDetailsManager.updateUser(updatedUser);
    }

    public void changeUserRole(String username, String[] newRoles) {
        UserDetails existingUser = userDetailsManager.loadUserByUsername(username);
        UserDetails updatedUser = org.springframework.security.core.userdetails.User.builder()
                .username(username)
                .password(existingUser.getPassword())
                .roles(newRoles)
                .disabled(!existingUser.isEnabled())
                .build();
        userDetailsManager.updateUser(updatedUser);
    }

    public void deleteUser(String username) {
        userDetailsManager.deleteUser(username);
    }

    public void disableUser(String username) {
        UserDetails existingUser = userDetailsManager.loadUserByUsername(username);
        UserDetails updatedUser = org.springframework.security.core.userdetails.User.builder()
                .username(username)
                .password(existingUser.getPassword())
                .authorities(existingUser.getAuthorities())
                .disabled(true)
                .build();
        userDetailsManager.updateUser(updatedUser);
    }

    public void enableUser(String username) {
        UserDetails existingUser = userDetailsManager.loadUserByUsername(username);
        UserDetails updatedUser = org.springframework.security.core.userdetails.User.builder()
                .username(username)
                .password(existingUser.getPassword())
                .authorities(existingUser.getAuthorities())
                .disabled(false)
                .build();
        userDetailsManager.updateUser(updatedUser);
    }

    // User Profile Management Methods
    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    public User saveUser(User user) {
        return userDao.save(user);
    }

    public User getUserById(String userId) {
        return userDao.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));
    }

    public User getUserByEmail(String email) {
        return userDao.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found with email: " + email));
    }

    public User updateUserEmail(String userId, String email) {
        User user = getUserById(userId);
        user.setEmail(email);
        return userDao.save(user);
    }

    public User updateUserPhoneNumber(String userId, String phoneNumber) {
        User user = getUserById(userId);
        user.setPhoneNumber(phoneNumber);
        return userDao.save(user);
    }



    // Address Management
    public User updateUserAddress(String userId, Address address) {
        User user = getUserById(userId);
        address.setUser(user);
        user.setAddress(address);
        return userDao.save(user);
    }

    // Favourites Management
    public User addToFavourites(String userId, Favourites favourite) {
        User user = getUserById(userId);
        favourite.setUser(user);
        user.getFavourites().add(favourite);
        return userDao.save(user);
    }

    public User removeFromFavourites(String userId, Long favouriteId) {
        User user = getUserById(userId);
        user.getFavourites().removeIf(f -> f.getId().equals(favouriteId));
        return userDao.save(user);
    }

    public List<Favourites> getUserFavourites(String userId) {
        User user = getUserById(userId);
        return user.getFavourites();
    }

    // Orders Management
    public User addOrder(String userId, Orders order) {
        User user = getUserById(userId);
        order.setUser(user);
        user.getOrders().add(order);
        return userDao.save(user);
    }

    public List<Orders> getUserOrders(String userId) {
        User user = getUserById(userId);
        return user.getOrders();
    }

    // Utility Methods
    public boolean existsByEmail(String email) {
        return userDao.existsByEmail(email);
    }

    public boolean existsById(String userId) {
        return userDao.existsById(userId);
    }
}