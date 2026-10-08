package com.restaurant.controller.admin;

import com.restaurant.dtos.ApiResponse;
import com.restaurant.dtos.admin.CategoryRequest;
import com.restaurant.dtos.admin.CategoryResponse;
import com.restaurant.dtos.PageResponse;
import com.restaurant.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final MenuService menuService;

    @GetMapping
    public ApiResponse<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categories = menuService.getAllCategories().stream()
                .map(this::mapToResponse)
                .toList();
        return ApiResponse.success(categories);
    }

    @GetMapping("/{id}")
    public ApiResponse<CategoryResponse> getCategory(@PathVariable Long id) {
        return ApiResponse.success(mapToResponse(menuService.getCategoryById(id)));
    }

    @PostMapping
    public ApiResponse<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = mapToResponse(menuService.createCategory(mapToDTO(request)));
        return ApiResponse.success("Category created successfully", response);
    }

    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = mapToResponse(menuService.updateCategory(id, mapToDTO(request)));
        return ApiResponse.success("Category updated successfully", response);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        menuService.deleteCategory(id);
        return ApiResponse.successMessage("Category deleted successfully");
    }

    private CategoryResponse mapToResponse(com.restaurant.dtos.customer.CategoryDTO dto) {
        return CategoryResponse.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .imageUrl(dto.getImageUrl())
                .displayOrder(dto.getDisplayOrder())
                .isActive(true)
                .menuItemsCount(0L)
                .build();
    }

    private com.restaurant.dtos.customer.CategoryDTO mapToDTO(CategoryRequest request) {
        return com.restaurant.dtos.customer.CategoryDTO.builder()
                .name(request.getName())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .displayOrder(request.getDisplayOrder())
                .build();
    }
}