package com.projects.cafe_winchester_backend.repository;

import com.projects.cafe_winchester_backend.entity.Payments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentDao extends JpaRepository<Payments, Long> {
}
