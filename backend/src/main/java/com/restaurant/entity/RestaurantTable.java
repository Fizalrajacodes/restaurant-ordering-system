package com.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "restaurant_tables", indexes = {
    @Index(name = "idx_tables_number", columnList = "table_number", unique = true),
    @Index(name = "idx_tables_status", columnList = "status"),
    @Index(name = "idx_tables_qr_token", columnList = "qr_token", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "table_number", nullable = false, unique = true, length = 20)
    private String tableNumber;

    @Column(name = "table_name", nullable = false, length = 50)
    private String tableName;

    @Column(nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TableStatus status;

    @Column(name = "qr_token", nullable = false, unique = true, length = 100)
    private String qrToken;

    @Column(name = "qr_code_url", length = 500)
    private String qrCodeUrl;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum TableStatus {
        AVAILABLE, OCCUPIED, RESERVED, MAINTENANCE, INACTIVE
    }
}