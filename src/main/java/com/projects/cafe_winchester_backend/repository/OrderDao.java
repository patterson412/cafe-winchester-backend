package com.projects.cafe_winchester_backend.repository;

import com.projects.cafe_winchester_backend.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDao extends JpaRepository<Orders, Long> {
}
