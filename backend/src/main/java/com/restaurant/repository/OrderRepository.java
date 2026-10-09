package com.restaurant.repository;

import com.restaurant.entity.Order;
import com.restaurant.entity.RestaurantTable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.menuItem WHERE o.orderNumber = :orderNumber")
    Optional<Order> findByOrderNumberWithItems(@Param("orderNumber") String orderNumber);

    List<Order> findByTableAndOrderStatusIn(RestaurantTable table, List<Order.OrderStatus> statuses);

    List<Order> findByOrderStatusInOrderByCreatedAtDesc(List<Order.OrderStatus> statuses);

    Page<Order> findByOrderStatusIn(List<Order.OrderStatus> statuses, Pageable pageable);

    Page<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Order> findByCustomerMobileAndCreatedAtBetween(String mobile, LocalDateTime start, LocalDateTime end);

    Optional<Order> findByOrderNumberAndCustomerMobile(String orderNumber, String mobile);

    @Query("SELECT o FROM Order o WHERE o.table = :table AND o.orderStatus IN :statuses ORDER BY o.createdAt DESC")
    List<Order> findActiveOrdersByTable(@Param("table") RestaurantTable table, @Param("statuses") List<Order.OrderStatus> statuses);

    @Query("SELECT o FROM Order o WHERE o.orderStatus IN :statuses ORDER BY o.createdAt ASC")
    List<Order> findActiveOrdersForKitchen(@Param("statuses") List<Order.OrderStatus> statuses);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :startDate AND o.orderStatus != :cancelledStatus")
    long countOrdersSince(@Param("startDate") LocalDateTime startDate, @Param("cancelledStatus") Order.OrderStatus cancelledStatus);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.createdAt >= :startDate AND o.paymentStatus = :paidStatus")
    BigDecimal getRevenueSince(@Param("startDate") LocalDateTime startDate, @Param("paidStatus") Order.PaymentStatus paidStatus);

    @Query("SELECT o FROM Order o WHERE o.orderStatus = :status ORDER BY o.createdAt ASC")
    List<Order> findByOrderStatusOrderByCreatedAtAsc(@Param("status") Order.OrderStatus status);

    boolean existsByOrderNumber(String orderNumber);

    long countByOrderStatus(Order.OrderStatus status);

    long countByPaymentStatus(Order.PaymentStatus status);
}