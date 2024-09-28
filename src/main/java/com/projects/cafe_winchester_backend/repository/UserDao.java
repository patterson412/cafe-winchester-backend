package com.projects.cafe_winchester_backend.repository;

import com.projects.cafe_winchester_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDao extends JpaRepository<User, Long> {
}
