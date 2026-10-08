package com.restaurant.repository;

import com.restaurant.entity.MenuItem;
import com.restaurant.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByCategoryAndIsAvailableTrueOrderByDisplayOrderAsc(Category category);

    List<MenuItem> findByIsAvailableTrueOrderByCategoryDisplayOrderAscDisplayOrderAsc();

    List<MenuItem> findByIsFeaturedTrueAndIsAvailableTrue();

    Page<MenuItem> findByCategoryAndIsAvailableTrue(Category category, Pageable pageable);

    Page<MenuItem> findByIsAvailableTrue(Pageable pageable);

    @Query("SELECT m FROM MenuItem m WHERE m.category = :category AND m.isAvailable = true ORDER BY m.displayOrder ASC")
    List<MenuItem> findAvailableByCategory(@Param("category") Category category);

    @Query("SELECT m FROM MenuItem m WHERE m.name ILIKE %:search% AND m.isAvailable = true ORDER BY m.name ASC")
    List<MenuItem> searchByName(@Param("search") String search);

    @Query("SELECT m FROM MenuItem m WHERE m.category = :category AND m.isAvailable = true AND m.name ILIKE %:search% ORDER BY m.displayOrder ASC")
    List<MenuItem> searchByCategoryAndName(@Param("category") Category category, @Param("search") String search);

    List<MenuItem> findByCategoryAndFoodTypeAndIsAvailableTrue(Category category, MenuItem.FoodType foodType);

    boolean existsByNameAndCategory(String name, Category category);

    long countByCategoryAndIsAvailableTrue(Category category);
}