package com.restaurant.repository;

import com.restaurant.entity.Order;
import com.restaurant.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {

    List<OrderStatusHistory> findByOrderOrderByChangedAtAsc(Order order);

    @Query("SELECT h FROM OrderStatusHistory h WHERE h.order = :order ORDER BY h.changedAt ASC")
    List<OrderStatusHistory> findByOrderWithDetails(@Param("order") Order order);
}