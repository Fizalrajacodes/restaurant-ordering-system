package com.restaurant.config;

import com.restaurant.entity.Category;
import com.restaurant.entity.MenuItem;
import com.restaurant.entity.RestaurantTable;
import com.restaurant.entity.User;
import com.restaurant.repository.CategoryRepository;
import com.restaurant.repository.MenuItemRepository;
import com.restaurant.repository.RestaurantTableRepository;
import com.restaurant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final RestaurantTableRepository tableRepository;
    private final CategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            log.info("Initializing default data...");

            // Create admin user if not exists, or ensure the default password is correct
            var adminOpt = userRepository.findByEmail("admin@restaurant.com");
            User admin;
            if (adminOpt.isEmpty()) {
                admin = User.builder()
                        .name("Admin User")
                        .email("admin@restaurant.com")
                        .mobile("9876543210")
                        .passwordHash(passwordEncoder.encode("admin123"))
                        .role(User.Role.ADMIN)
                        .isActive(true)
                        .build();
                log.info("Default admin user created: admin@restaurant.com / admin123");
            } else {
                admin = adminOpt.get();
                log.info("Admin user found, ensuring password is correct and account is active");
            }
            // Always ensure password is correct and account is active
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setIsActive(true);
            userRepository.save(admin);
            log.info("Default admin password set to: admin123, account activated");

            // Create sample tables if not exists
            if (tableRepository.count() == 0) {
                List<RestaurantTable> tables = List.of(
                    createTable("TABLE-01", "Table 01", 4),
                    createTable("TABLE-02", "Table 02", 4),
                    createTable("TABLE-03", "Table 03", 2),
                    createTable("TABLE-04", "Table 04", 6),
                    createTable("TABLE-05", "Table 05", 4),
                    createTable("TABLE-06", "Table 06", 2),
                    createTable("TABLE-07", "Table 07", 4),
                    createTable("TABLE-08", "Table 08", 8),
                    createTable("TABLE-09", "Table 09", 4),
                    createTable("TABLE-10", "Table 10", 2)
                );
                tableRepository.saveAll(tables);
                log.info("Created {} sample tables", tables.size());
            }

            // Create sample categories if not exists
            if (categoryRepository.count() == 0) {
                List<Category> categories = List.of(
                    createCategory("Starters", "Delicious appetizers to start your meal", 1),
                    createCategory("Biryani", "Aromatic and flavorful biryanis", 2),
                    createCategory("Main Course", "Hearty main dishes", 3),
                    createCategory("Beverages", "Refreshing drinks", 4),
                    createCategory("Desserts", "Sweet treats to end your meal", 5)
                );
                categoryRepository.saveAll(categories);
                log.info("Created {} sample categories", categories.size());

                // Create sample menu items
                createMenuItems();
            }

            log.info("Default data initialization completed");
        };
    }

    private RestaurantTable createTable(String tableNumber, String tableName, int capacity) {
        return RestaurantTable.builder()
                .tableNumber(tableNumber)
                .tableName(tableName)
                .capacity(capacity)
                .status(RestaurantTable.TableStatus.AVAILABLE)
                .isActive(true)
                .qrToken("QR-" + tableNumber + "-" + System.currentTimeMillis())
                .build();
    }

    private Category createCategory(String name, String description, int displayOrder) {
        return Category.builder()
                .name(name)
                .description(description)
                .displayOrder(displayOrder)
                .isActive(true)
                .build();
    }

    private void createMenuItems() {
        Category starters = categoryRepository.findByName("Starters")
                .orElseThrow(() -> new RuntimeException("Starters category not found"));
        Category biryani = categoryRepository.findByName("Biryani")
                .orElseThrow(() -> new RuntimeException("Biryani category not found"));
        Category mainCourse = categoryRepository.findByName("Main Course")
                .orElseThrow(() -> new RuntimeException("Main Course category not found"));
        Category beverages = categoryRepository.findByName("Beverages")
                .orElseThrow(() -> new RuntimeException("Beverages category not found"));
        Category desserts = categoryRepository.findByName("Desserts")
                .orElseThrow(() -> new RuntimeException("Desserts category not found"));

        List<MenuItem> menuItems = List.of(
            // Starters
            createMenuItem(starters, "Chicken 65", "Spicy crispy chicken with curry leaves", new BigDecimal("180.00"), MenuItem.FoodType.NON_VEG, 15, true, 1),
            createMenuItem(starters, "Paneer 65", "Crispy paneer cubes with spices", new BigDecimal("150.00"), MenuItem.FoodType.VEG, 15, true, 2),
            createMenuItem(starters, "Veg Manchurian", "Crispy vegetables with Manchurian sauce", new BigDecimal("140.00"), MenuItem.FoodType.VEG, 15, false, 3),
            createMenuItem(starters, "Chicken Lollipop", "Frenched chicken wings with spicy coating", new BigDecimal("200.00"), MenuItem.FoodType.NON_VEG, 20, false, 4),

            // Biryani
            createMenuItem(biryani, "Chicken Biryani", "Traditional chicken biryani with basmati rice", new BigDecimal("220.00"), MenuItem.FoodType.NON_VEG, 25, true, 1),
            createMenuItem(biryani, "Mutton Biryani", "Tender mutton biryani with aromatic spices", new BigDecimal("280.00"), MenuItem.FoodType.NON_VEG, 30, true, 2),
            createMenuItem(biryani, "Veg Biryani", "Mixed vegetable biryani with fragrant rice", new BigDecimal("180.00"), MenuItem.FoodType.VEG, 20, false, 3),
            createMenuItem(biryani, "Prawn Biryani", "Succulent prawns with basmati rice", new BigDecimal("320.00"), MenuItem.FoodType.NON_VEG, 25, false, 4),

            // Main Course
            createMenuItem(mainCourse, "Butter Chicken", "Creamy tomato-based chicken curry", new BigDecimal("240.00"), MenuItem.FoodType.NON_VEG, 20, true, 1),
            createMenuItem(mainCourse, "Paneer Butter Masala", "Cottage cheese in rich tomato gravy", new BigDecimal("200.00"), MenuItem.FoodType.VEG, 15, true, 2),
            createMenuItem(mainCourse, "Chicken Curry", "Traditional spicy chicken curry", new BigDecimal("220.00"), MenuItem.FoodType.NON_VEG, 20, false, 3),
            createMenuItem(mainCourse, "Dal Makhani", "Slow-cooked black lentils with cream", new BigDecimal("160.00"), MenuItem.FoodType.VEG, 15, false, 4),

            // Beverages
            createMenuItem(beverages, "Fresh Lime Soda", "Refreshing lime with soda", new BigDecimal("60.00"), MenuItem.FoodType.VEG, 5, false, 1),
            createMenuItem(beverages, "Coke", "Classic Coca-Cola", new BigDecimal("50.00"), MenuItem.FoodType.VEG, 2, false, 2),
            createMenuItem(beverages, "Sprite", "Lemon-lime soda", new BigDecimal("50.00"), MenuItem.FoodType.VEG, 2, false, 3),
            createMenuItem(beverages, "Masala Chai", "Spiced Indian tea", new BigDecimal("40.00"), MenuItem.FoodType.VEG, 5, false, 4),
            createMenuItem(beverages, "Cold Coffee", "Iced coffee with cream", new BigDecimal("80.00"), MenuItem.FoodType.VEG, 5, false, 5),
            createMenuItem(beverages, "Mango Lassi", "Sweet mango yogurt drink", new BigDecimal("90.00"), MenuItem.FoodType.VEG, 5, false, 6),

            // Desserts
            createMenuItem(desserts, "Gulab Jamun", "Deep-fried milk solids in sugar syrup", new BigDecimal("80.00"), MenuItem.FoodType.VEG, 10, true, 1),
            createMenuItem(desserts, "Ice Cream", "Vanilla/Chocolate/Strawberry ice cream", new BigDecimal("100.00"), MenuItem.FoodType.VEG, 5, true, 2),
            createMenuItem(desserts, "Rasmalai", "Soft cheese balls in saffron milk", new BigDecimal("120.00"), MenuItem.FoodType.VEG, 10, false, 3),
            createMenuItem(desserts, "Chocolate Brownie", "Warm chocolate brownie with ice cream", new BigDecimal("150.00"), MenuItem.FoodType.VEG, 10, false, 4)
        );

        menuItemRepository.saveAll(menuItems);
        log.info("Created {} sample menu items", menuItems.size());
    }

    private MenuItem createMenuItem(Category category, String name, String description, BigDecimal price,
                                     MenuItem.FoodType foodType, int prepTime, boolean featured, int displayOrder) {
        return MenuItem.builder()
                .category(category)
                .name(name)
                .description(description)
                .price(price)
                .foodType(foodType)
                .isAvailable(true)
                .preparationTimeMinutes(prepTime)
                .isFeatured(featured)
                .displayOrder(displayOrder)
                .build();
    }
}