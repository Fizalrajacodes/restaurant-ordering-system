package com.restaurant.repository;

import com.restaurant.entity.QRCode;
import com.restaurant.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QRCodeRepository extends JpaRepository<QRCode, Long> {

    Optional<QRCode> findByTable(RestaurantTable table);

    Optional<QRCode> findByToken(String token);

    boolean existsByToken(String token);

    Optional<QRCode> findByTableAndIsActiveTrue(RestaurantTable table);
}