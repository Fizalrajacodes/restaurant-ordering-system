-- Insert default admin user (password: admin123)
INSERT INTO users (name, email, mobile, password_hash, role, is_active) VALUES
('Admin User', 'admin@restaurant.com', '9876543210', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj/RK.PZvO.S', 'ADMIN', TRUE);

-- Insert sample tables
INSERT INTO restaurant_tables (table_number, table_name, capacity, status, is_active) VALUES
('TABLE-01', 'Table 01', 4, 'AVAILABLE', TRUE),
('TABLE-02', 'Table 02', 4, 'AVAILABLE', TRUE),
('TABLE-03', 'Table 03', 2, 'AVAILABLE', TRUE),
('TABLE-04', 'Table 04', 6, 'AVAILABLE', TRUE),
('TABLE-05', 'Table 05', 4, 'AVAILABLE', TRUE),
('TABLE-06', 'Table 06', 2, 'AVAILABLE', TRUE),
('TABLE-07', 'Table 07', 4, 'AVAILABLE', TRUE),
('TABLE-08', 'Table 08', 8, 'AVAILABLE', TRUE),
('TABLE-09', 'Table 09', 4, 'AVAILABLE', TRUE),
('TABLE-10', 'Table 10', 2, 'AVAILABLE', TRUE);

-- Insert sample categories
INSERT INTO categories (name, description, display_order, is_active) VALUES
('Starters', 'Delicious appetizers to start your meal', 1, TRUE),
('Biryani', 'Aromatic and flavorful biryanis', 2, TRUE),
('Main Course', 'Hearty main dishes', 3, TRUE),
('Beverages', 'Refreshing drinks', 4, TRUE),
('Desserts', 'Sweet treats to end your meal', 5, TRUE);

-- Insert sample menu items
INSERT INTO menu_items (category_id, name, description, price, food_type, is_available, preparation_time_minutes, is_featured, display_order) VALUES
-- Starters
(1, 'Chicken 65', 'Spicy crispy chicken with curry leaves', 180.00, 'NON_VEG', TRUE, 15, TRUE, 1),
(1, 'Paneer 65', 'Crispy paneer cubes with spices', 150.00, 'VEG', TRUE, 15, TRUE, 2),
(1, 'Veg Manchurian', 'Crispy vegetables with Manchurian sauce', 140.00, 'VEG', TRUE, 15, FALSE, 3),
(1, 'Chicken Lollipop', 'Frenched chicken wings with spicy coating', 200.00, 'NON_VEG', TRUE, 20, FALSE, 4),

-- Biryani
(2, 'Chicken Biryani', 'Traditional chicken biryani with basmati rice', 220.00, 'NON_VEG', TRUE, 25, TRUE, 1),
(2, 'Mutton Biryani', 'Tender mutton biryani with aromatic spices', 280.00, 'NON_VEG', TRUE, 30, TRUE, 2),
(2, 'Veg Biryani', 'Mixed vegetable biryani with fragrant rice', 180.00, 'VEG', TRUE, 20, FALSE, 3),
(2, 'Prawn Biryani', 'Succulent prawns with basmati rice', 320.00, 'NON_VEG', TRUE, 25, FALSE, 4),

-- Main Course
(3, 'Butter Chicken', 'Creamy tomato-based chicken curry', 240.00, 'NON_VEG', TRUE, 20, TRUE, 1),
(3, 'Paneer Butter Masala', 'Cottage cheese in rich tomato gravy', 200.00, 'VEG', TRUE, 15, TRUE, 2),
(3, 'Chicken Curry', 'Traditional spicy chicken curry', 220.00, 'NON_VEG', TRUE, 20, FALSE, 3),
(3, 'Dal Makhani', 'Slow-cooked black lentils with cream', 160.00, 'VEG', TRUE, 15, FALSE, 4),
(3, 'Fish Curry', 'Tangy fish curry with tamarind', 260.00, 'NON_VEG', TRUE, 20, FALSE, 5),

-- Beverages
(4, 'Fresh Lime Soda', 'Refreshing lime with soda', 60.00, 'VEG', TRUE, 5, FALSE, 1),
(4, 'Coke', 'Classic Coca-Cola', 50.00, 'VEG', TRUE, 2, FALSE, 2),
(4, 'Sprite', 'Lemon-lime soda', 50.00, 'VEG', TRUE, 2, FALSE, 3),
(4, 'Masala Chai', 'Spiced Indian tea', 40.00, 'VEG', TRUE, 5, FALSE, 4),
(4, 'Cold Coffee', 'Iced coffee with cream', 80.00, 'VEG', TRUE, 5, FALSE, 5),
(4, 'Mango Lassi', 'Sweet mango yogurt drink', 90.00, 'VEG', TRUE, 5, FALSE, 6),

-- Desserts
(5, 'Gulab Jamun', 'Deep-fried milk solids in sugar syrup', 80.00, 'VEG', TRUE, 10, TRUE, 1),
(5, 'Ice Cream', 'Vanilla/Chocolate/Strawberry ice cream', 100.00, 'VEG', TRUE, 5, TRUE, 2),
(5, 'Rasmalai', 'Soft cheese balls in saffron milk', 120.00, 'VEG', TRUE, 10, FALSE, 3),
(5, 'Chocolate Brownie', 'Warm chocolate brownie with ice cream', 150.00, 'VEG', TRUE, 10, FALSE, 4);