package com.projects.cafe_winchester_backend.repository;

import com.projects.cafe_winchester_backend.entity.OrderStatus;
import com.projects.cafe_winchester_backend.entity.Orders;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderDao extends JpaRepository<Orders, Long> {
    Optional<Orders> findFirstByUserUserIdOrderByOrderDateDesc(String userId);

    /* SQL Equivalent
    SELECT * FROM Orders
    WHERE user_id = ?
    ORDER BY order_date DESC
    LIMIT 1
     */



    List<Orders> findByStatusNotInOrderByOrderDateDesc(List<OrderStatus> statuses);

    /* SQL Equivalent
    SELECT * FROM Orders
    WHERE status NOT IN (?)
    ORDER BY order_date DESC
     */


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    Optional<Orders> findByOrderId(Long orderId);
}
