package com.restaurant.controller.customer;

import com.restaurant.dtos.ApiResponse;
import com.restaurant.dtos.customer.CartDTO;
import com.restaurant.dtos.customer.CategoryDTO;
import com.restaurant.dtos.customer.MenuItemDTO;
import com.restaurant.dtos.customer.TableDTO;
import com.restaurant.service.MenuService;
import com.restaurant.service.QRCodeService;
import com.restaurant.service.TableService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;
    private final TableService tableService;
    private final QRCodeService qrCodeService;

    @GetMapping("/categories")
    public ApiResponse<List<CategoryDTO>> getCategories() {
        return ApiResponse.success(menuService.getAllCategories());
    }

    @GetMapping("/categories/{id}")
    public ApiResponse<CategoryDTO> getCategory(@PathVariable Long id) {
        return ApiResponse.success(menuService.getCategoryById(id));
    }

    @GetMapping("/items")
    public ApiResponse<List<MenuItemDTO>> getAllItems() {
        return ApiResponse.success(menuService.getAllAvailableMenuItems());
    }

    @GetMapping("/items/featured")
    public ApiResponse<List<MenuItemDTO>> getFeaturedItems() {
        return ApiResponse.success(menuService.getFeaturedItems());
    }

    @GetMapping("/categories/{categoryId}/items")
    public ApiResponse<List<MenuItemDTO>> getItemsByCategory(@PathVariable Long categoryId) {
        return ApiResponse.success(menuService.getMenuItemsByCategory(categoryId));
    }

    @GetMapping("/items/search")
    public ApiResponse<List<MenuItemDTO>> searchItems(@RequestParam String q) {
        return ApiResponse.success(menuService.searchMenuItems(q));
    }

    @GetMapping("/items/{id}")
    public ApiResponse<MenuItemDTO> getItem(@PathVariable Long id) {
        return ApiResponse.success(menuService.getMenuItemById(id));
    }

    @GetMapping("/table/{token}")
    public ApiResponse<TableDTO> getTableByToken(@PathVariable String token) {
        qrCodeService.recordScan(token);
        return ApiResponse.success(tableService.getTableByToken(token));
    }

    @GetMapping("/table/number/{tableNumber}")
    public ApiResponse<TableDTO> getTableByNumber(@PathVariable String tableNumber) {
        return ApiResponse.success(tableService.getTableByNumber(tableNumber));
    }
}