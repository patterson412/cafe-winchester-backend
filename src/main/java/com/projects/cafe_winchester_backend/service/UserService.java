package com.projects.cafe_winchester_backend.service;

import com.projects.cafe_winchester_backend.entity.Address;
import com.projects.cafe_winchester_backend.entity.Favourites;
import com.projects.cafe_winchester_backend.entity.Orders;
import com.projects.cafe_winchester_backend.entity.User;
import com.projects.cafe_winchester_backend.repository.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class UserService {


    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    // Create
    public User createUser(User user) {
        return userDao.save(user);
    }

    // Read
    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    public User getUserById(Long userId) {
        return userDao.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));
    }

    /* Below is the code for the above function in the Old without arrow function
    public User getUserById(Long userId) {
        Optional<User> userOptional = userDao.findById(userId);
        return userOptional.orElseThrow(new Supplier<NoSuchElementException>() {    //the <NoSuchElementException> is specifying the generic type parameter for the Supplier interface, indicating what type of object the Supplier will produce/return.
            @Override
            public NoSuchElementException get() {
                return new NoSuchElementException("User not found with id: " + userId);
            }
        });
    }*/

    public User getUserByEmail(String email) {
        return userDao.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found with email: " + email));
    }

    // Update
    public User updateUser(Long userId, User userDetails) {
        User user = getUserById(userId);

        user.setEmail(userDetails.getEmail());
        user.setPhoneNumber(userDetails.getPhoneNumber());

        return userDao.save(user);
    }

    // Delete
    public void deleteUser(Long userId) {
        if (!userDao.existsById(userId)) {
            throw new NoSuchElementException("User not found with id: " + userId);
        }
        userDao.deleteById(userId);
    }

    // Address management
    public User updateUserAddress(Long userId, Address address) {
        User user = getUserById(userId);
        address.setUser(user);
        user.setAddress(address);
        return userDao.save(user);
    }

    // Favourites management
    public User addToFavourites(Long userId, Favourites favourite) {
        User user = getUserById(userId);
        favourite.setUser(user);
        user.getFavourites().add(favourite);
        return userDao.save(user);
    }

    public User removeFromFavourites(Long userId, Long favouriteId) {
        User user = getUserById(userId);
        user.getFavourites().removeIf(f -> f.getId().equals(favouriteId));
        return userDao.save(user);
    }

    public List<Favourites> getUserFavourites(Long userId) {
        User user = getUserById(userId);
        return user.getFavourites();
    }

    // Orders management
    public User addOrder(Long userId, Orders order) {
        User user = getUserById(userId);
        order.setUser(user);
        user.getOrders().add(order);
        return userDao.save(user);
    }

    public List<Orders> getUserOrders(Long userId) {
        User user = getUserById(userId);
        return user.getOrders();
    }

    // Utility methods
    public boolean existsByEmail(String email) {
        return userDao.existsByEmail(email);
    }

    public boolean existsById(Long userId) {
        return userDao.existsById(userId);
    }



}
