package com.projects.cafe_winchester_backend.repository;

import com.projects.cafe_winchester_backend.entity.Payments;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentDao extends JpaRepository<Payments, Long> {
}
