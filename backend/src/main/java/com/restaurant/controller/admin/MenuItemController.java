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
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

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

    @PostMapping("/{id}/image")
    public ApiResponse<String> uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty()) {
                return ApiResponse.error("File is empty");
            }
            
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ApiResponse.error("File must be an image");
            }
            
            if (file.getSize() > 5 * 1024 * 1024) { // 5MB limit
                return ApiResponse.error("File size must be less than 5MB");
            }

            // Create upload directory
            String uploadDir = "uploads/menu-items";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(filename);

            // Save file
            Files.copy(file.getInputStream(), filePath);

            // Update menu item with image URL
            String imageUrl = "/uploads/menu-items/" + filename;
            MenuItemDTO dto = menuService.getMenuItemById(id);
            dto.setImageUrl(imageUrl);
            menuService.updateMenuItem(id, dto);

            return ApiResponse.success("Image uploaded successfully", imageUrl);
        } catch (IOException e) {
            return ApiResponse.error("Failed to upload image: " + e.getMessage());
        }
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