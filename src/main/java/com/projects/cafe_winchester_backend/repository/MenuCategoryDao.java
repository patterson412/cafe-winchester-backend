package com.projects.cafe_winchester_backend.repository;

import com.projects.cafe_winchester_backend.entity.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuCategoryDao extends JpaRepository<MenuCategory, Long> {
}
