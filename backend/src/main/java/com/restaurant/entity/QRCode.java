package com.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "qr_codes", indexes = {
    @Index(name = "idx_qr_codes_table", columnList = "table_id", unique = true),
    @Index(name = "idx_qr_codes_token", columnList = "token", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QRCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false, unique = true)
    private RestaurantTable table;

    @Column(nullable = false, unique = true, length = 100)
    private String token;

    @Column(name = "qr_data", nullable = false, columnDefinition = "TEXT")
    private String qrData;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "image_data", columnDefinition = "BYTEA")
    private byte[] imageData;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "scan_count")
    @Builder.Default
    private Integer scanCount = 0;

    @Column(name = "last_scanned_at")
    private LocalDateTime lastScannedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}