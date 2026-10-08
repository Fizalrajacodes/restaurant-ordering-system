package com.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_orders_number", columnList = "order_number", unique = true),
    @Index(name = "idx_orders_table", columnList = "table_id"),
    @Index(name = "idx_orders_status", columnList = "order_status"),
    @Index(name = "idx_orders_payment_status", columnList = "payment_status"),
    @Index(name = "idx_orders_customer_mobile", columnList = "customer_mobile"),
    @Index(name = "idx_orders_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private RestaurantTable table;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "customer_mobile", nullable = false, length = 20)
    private String customerMobile;

    @Column(name = "customer_email", length = 150)
    private String customerEmail;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal tax;

    @Column(name = "discount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 20)
    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Column(name = "special_instructions", length = 1000)
    private String specialInstructions;

    @Column(name = "estimated_ready_time")
    private LocalDateTime estimatedReadyTime;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "preparing_at")
    private LocalDateTime preparingAt;

    @Column(name = "ready_at")
    private LocalDateTime readyAt;

    @Column(name = "served_at")
    private LocalDateTime servedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancelled_reason", length = 500)
    private String cancelledReason;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum OrderStatus {
        PENDING, CONFIRMED, PREPARING, READY, SERVED, CANCELLED
    }

    public enum PaymentStatus {
        PAYMENT_PENDING, PAYMENT_COMPLETED, PAYMENT_FAILED, REFUNDED, PARTIAL_REFUND
    }
}