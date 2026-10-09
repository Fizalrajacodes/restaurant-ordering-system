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
                .imageUrl(getImageUrlForItem(name))
                .foodType(foodType)
                .isAvailable(true)
                .preparationTimeMinutes(prepTime)
                .isFeatured(featured)
                .displayOrder(displayOrder)
                .build();
    }
    
    private String getImageUrlForItem(String name) {
        // Using placeholder images from a free service
        String slug = name.toLowerCase().replaceAll("[^a-z0-9]+", "-");
        return "https://images.unsplash.com/photo-" + getImageId(slug) + "?w=400&h=300&fit=crop";
    }
    
    private String getImageId(String slug) {
        // Map common food items to Unsplash photo IDs
        return switch (slug) {
            case "chicken-65" -> "1599487488170-d11ec9c172f0";
            case "paneer-65" -> "1585476980887-1d43b7f703c5";
            case "veg-manchurian" -> "1603133872878-684f208fb84b";
            case "chicken-lollipop" -> "1598103440144-83863016a988";
            case "chicken-biryani" -> "1563379926897-6a4c0721c18f";
            case "mutton-biryani" -> "1589301374813-1a9e1b0d8a6b";
            case "veg-biryani" -> "1596797038530-2c1072296d4b";
            case "prawn-biryani" -> "1565680018434-b513d5e5fd47";
            case "butter-chicken" -> "1588166524941-3bf61b5c4c8a";
            case "paneer-butter-masala" -> "1631452180539-96aca7d480b5";
            case "chicken-curry" -> "1585937421618-67d9959d59e4";
            case "dal-makhani" -> "1546833999-b9f581a1996d";
            case "fresh-lime-soda" -> "1544145945-6472d11e4e13";
            case "coke" -> "1554866585-cd94860890b7";
            case "sprite" -> "1544145945-6472d11e4e13";
            case "masala-chai" -> "1571934811356-5cc061b6821f";
            case "cold-coffee" -> "1511920170033-f8396924c348";
            case "mango-lassi" -> "1570075060335-17636629828a";
            case "gulab-jamun" -> "1565958011703-44f9829ba187";
            case "ice-cream" -> "1563805042-7684c019e1cb";
            case "rasmalai" -> "1565958011703-44f9829ba187";
            case "chocolate-brownie" -> "1606313564200-e75d5e30476c";
            default -> "1567620905732-2d1ec7ab7445"; // generic food
        };
    }
}