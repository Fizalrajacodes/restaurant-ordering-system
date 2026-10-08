package com.restaurant.repository;

import com.restaurant.entity.Order;
import com.restaurant.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrder(Order order);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.order = :order")
    List<OrderItem> findByOrderWithDetails(@Param("order") Order order);

    @Query("SELECT oi.itemName, SUM(oi.quantity) as totalQty, SUM(oi.totalPrice) as totalRevenue " +
           "FROM OrderItem oi JOIN oi.order o " +
           "WHERE o.createdAt >= :startDate AND o.orderStatus != :cancelledStatus " +
           "GROUP BY oi.itemName ORDER BY totalQty DESC")
    List<Object[]> getMostOrderedItems(@Param("startDate") LocalDateTime startDate, 
                                       @Param("cancelledStatus") Order.OrderStatus cancelledStatus);

    @Query("SELECT oi.itemName, SUM(oi.quantity) as totalQty " +
           "FROM OrderItem oi JOIN oi.order o " +
           "WHERE o.createdAt >= :startDate AND o.orderStatus != :cancelledStatus " +
           "GROUP BY oi.itemName ORDER BY totalQty ASC")
    List<Object[]> getLeastOrderedItems(@Param("startDate") LocalDateTime startDate,
                                        @Param("cancelledStatus") Order.OrderStatus cancelledStatus);
}