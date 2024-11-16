package com.projects.cafe_winchester_backend.repository;

import com.projects.cafe_winchester_backend.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MenuItemDao extends JpaRepository<MenuItem, Long> {
    Optional<MenuItem> findByNameContainingIgnoreCase(String name);
}
