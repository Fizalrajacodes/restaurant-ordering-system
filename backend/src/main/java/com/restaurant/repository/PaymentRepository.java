package com.restaurant.repository;

import com.restaurant.entity.Order;
import com.restaurant.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrder(Order order);

    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    Optional<Payment> findByRazorpayPaymentId(String razorpayPaymentId);

    List<Payment> findByOrderId(Long orderId);

    List<Payment> findByPaymentStatus(Payment.PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.razorpayOrderId = :orderId AND p.paymentStatus = :status")
    Optional<Payment> findByRazorpayOrderIdAndStatus(@Param("orderId") String orderId, @Param("status") Payment.PaymentStatus status);

    boolean existsByRazorpayOrderId(String razorpayOrderId);

    boolean existsByRazorpayPaymentId(String razorpayPaymentId);
}