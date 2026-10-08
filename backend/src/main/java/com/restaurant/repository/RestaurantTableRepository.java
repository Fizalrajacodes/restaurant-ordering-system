package com.restaurant.repository;

import com.restaurant.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {

    Optional<RestaurantTable> findByTableNumber(String tableNumber);

    Optional<RestaurantTable> findByQrToken(String qrToken);

    List<RestaurantTable> findByIsActiveTrueOrderByTableNumberAsc();

    List<RestaurantTable> findByStatus(RestaurantTable.TableStatus status);

    @Query("SELECT t FROM RestaurantTable t WHERE t.qrToken = :token AND t.isActive = true")
    Optional<RestaurantTable> findActiveByQrToken(@Param("token") String token);

    boolean existsByTableNumber(String tableNumber);

    boolean existsByQrToken(String qrToken);

    long countByIsActiveTrue();
}