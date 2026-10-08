package com.restaurant.controller.admin;

import com.restaurant.dtos.ApiResponse;
import com.restaurant.dtos.admin.MenuItemRequest;
import com.restaurant.dtos.admin.MenuItemResponse;
import com.restaurant.dtos.customer.MenuItemDTO;
import com.restaurant.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/menu-items")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuService menuService;

    @GetMapping
    public ApiResponse<List<MenuItemResponse>> getAllMenuItems() {
        List<MenuItemResponse> items = menuService.getAllAvailableMenuItems().stream()
                .map(this::mapToResponse)
                .toList();
        return ApiResponse.success(items);
    }

    @GetMapping("/{id}")
    public ApiResponse<MenuItemResponse> getMenuItem(@PathVariable Long id) {
        return ApiResponse.success(mapToResponse(menuService.getMenuItemById(id)));
    }

    @GetMapping("/category/{categoryId}")
    public ApiResponse<List<MenuItemResponse>> getMenuItemsByCategory(@PathVariable Long categoryId) {
        List<MenuItemResponse> items = menuService.getMenuItemsByCategory(categoryId).stream()
                .map(this::mapToResponse)
                .toList();
        return ApiResponse.success(items);
    }

    @PostMapping
    public ApiResponse<MenuItemResponse> createMenuItem(@Valid @RequestBody MenuItemRequest request) {
        MenuItemDTO dto = mapToDTO(request);
        MenuItemResponse response = mapToResponse(menuService.createMenuItem(dto));
        return ApiResponse.success("Menu item created successfully", response);
    }

    @PutMapping("/{id}")
    public ApiResponse<MenuItemResponse> updateMenuItem(@PathVariable Long id, @Valid @RequestBody MenuItemRequest request) {
        MenuItemDTO dto = mapToDTO(request);
        MenuItemResponse response = mapToResponse(menuService.updateMenuItem(id, dto));
        return ApiResponse.success("Menu item updated successfully", response);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteMenuItem(@PathVariable Long id) {
        menuService.deleteMenuItem(id);
        return ApiResponse.successMessage("Menu item deleted successfully");
    }

    private MenuItemResponse mapToResponse(MenuItemDTO dto) {
        return MenuItemResponse.builder()
                .id(dto.getId())
                .categoryId(dto.getCategoryId())
                .categoryName(dto.getCategoryName())
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .imageUrl(dto.getImageUrl())
                .foodType(dto.getFoodType())
                .isAvailable(dto.getIsAvailable())
                .preparationTimeMinutes(dto.getPreparationTimeMinutes())
                .isFeatured(dto.getIsFeatured())
                .displayOrder(dto.getDisplayOrder())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    private MenuItemDTO mapToDTO(MenuItemRequest request) {
        return MenuItemDTO.builder()
                .categoryId(request.getCategoryId())
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .imageUrl(request.getImageUrl())
                .foodType(request.getFoodType().name())
                .isAvailable(request.getIsAvailable())
                .preparationTimeMinutes(request.getPreparationTimeMinutes())
                .isFeatured(request.getIsFeatured())
                .displayOrder(request.getDisplayOrder())
                .build();
    }
}