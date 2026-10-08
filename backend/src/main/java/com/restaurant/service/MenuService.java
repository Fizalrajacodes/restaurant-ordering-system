package com.restaurant.service;

import com.restaurant.dtos.customer.CategoryDTO;
import com.restaurant.dtos.customer.MenuItemDTO;
import com.restaurant.entity.Category;
import com.restaurant.entity.MenuItem;
import com.restaurant.exception.ResourceNotFoundException;
import com.restaurant.repository.CategoryRepository;
import com.restaurant.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MenuService {

    private final CategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;

    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findActiveOrdered().stream()
                .map(this::mapToCategoryDTO)
                .collect(Collectors.toList());
    }

    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return mapToCategoryDTO(category);
    }

    public List<MenuItemDTO> getMenuItemsByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
        
        return menuItemRepository.findByCategoryAndIsAvailableTrueOrderByDisplayOrderAsc(category).stream()
                .map(this::mapToMenuItemDTO)
                .collect(Collectors.toList());
    }

    public List<MenuItemDTO> getAllAvailableMenuItems() {
        return menuItemRepository.findByIsAvailableTrueOrderByCategoryDisplayOrderAscDisplayOrderAsc().stream()
                .map(this::mapToMenuItemDTO)
                .collect(Collectors.toList());
    }

    public Page<MenuItemDTO> getAllAvailableMenuItemsPaged(Pageable pageable) {
        return menuItemRepository.findByIsAvailableTrue(pageable).map(this::mapToMenuItemDTO);
    }

    public List<MenuItemDTO> getFeaturedItems() {
        return menuItemRepository.findByIsFeaturedTrueAndIsAvailableTrue().stream()
                .map(this::mapToMenuItemDTO)
                .collect(Collectors.toList());
    }

    public List<MenuItemDTO> searchMenuItems(String query) {
        return menuItemRepository.searchByName(query).stream()
                .map(this::mapToMenuItemDTO)
                .collect(Collectors.toList());
    }

    public MenuItemDTO getMenuItemById(Long id) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + id));
        return mapToMenuItemDTO(item);
    }

    @Transactional
    public MenuItemDTO createMenuItem(MenuItemDTO dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId()));

        MenuItem item = MenuItem.builder()
                .category(category)
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .imageUrl(dto.getImageUrl())
                .foodType(MenuItem.FoodType.valueOf(dto.getFoodType()))
                .isAvailable(dto.getIsAvailable())
                .preparationTimeMinutes(dto.getPreparationTimeMinutes())
                .isFeatured(dto.getIsFeatured())
                .displayOrder(dto.getDisplayOrder())
                .build();

        item = menuItemRepository.save(item);
        return mapToMenuItemDTO(item);
    }

    @Transactional
    public MenuItemDTO updateMenuItem(Long id, MenuItemDTO dto) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + id));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId()));

        item.setCategory(category);
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setPrice(dto.getPrice());
        item.setImageUrl(dto.getImageUrl());
        item.setFoodType(MenuItem.FoodType.valueOf(dto.getFoodType()));
        item.setIsAvailable(dto.getIsAvailable());
        item.setPreparationTimeMinutes(dto.getPreparationTimeMinutes());
        item.setIsFeatured(dto.getIsFeatured());
        item.setDisplayOrder(dto.getDisplayOrder());

        item = menuItemRepository.save(item);
        return mapToMenuItemDTO(item);
    }

    @Transactional
    public void deleteMenuItem(Long id) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + id));
        menuItemRepository.delete(item);
    }

    @Transactional
    public CategoryDTO createCategory(CategoryDTO dto) {
        Category category = Category.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .imageUrl(dto.getImageUrl())
                .displayOrder(dto.getDisplayOrder())
                .isActive(true)
                .build();

        category = categoryRepository.save(category);
        return mapToCategoryDTO(category);
    }

    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setImageUrl(dto.getImageUrl());
        category.setDisplayOrder(dto.getDisplayOrder());
        category.setIsActive(dto.getIsActive());

        category = categoryRepository.save(category);
        return mapToCategoryDTO(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        categoryRepository.delete(category);
    }

    private CategoryDTO mapToCategoryDTO(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .displayOrder(category.getDisplayOrder())
                .isActive(category.getIsActive())
                .build();
    }

    private MenuItemDTO mapToMenuItemDTO(MenuItem item) {
        return MenuItemDTO.builder()
                .id(item.getId())
                .categoryId(item.getCategory().getId())
                .categoryName(item.getCategory().getName())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .imageUrl(item.getImageUrl())
                .foodType(item.getFoodType().name())
                .isAvailable(item.getIsAvailable())
                .preparationTimeMinutes(item.getPreparationTimeMinutes())
                .isFeatured(item.getIsFeatured())
                .displayOrder(item.getDisplayOrder())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}