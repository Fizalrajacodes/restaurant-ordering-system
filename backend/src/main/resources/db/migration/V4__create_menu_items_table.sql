CREATE TABLE menu_items (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL REFERENCES categories(id),
    name VARCHAR(150) NOT NULL,
    description VARCHAR(1000),
    price DECIMAL(10,2) NOT NULL,
    image_url VARCHAR(500),
    food_type VARCHAR(20) NOT NULL CHECK (food_type IN ('VEG', 'NON_VEG', 'VEGAN', 'GLUTEN_FREE')),
    is_available BOOLEAN DEFAULT TRUE,
    preparation_time_minutes INTEGER DEFAULT 15,
    is_featured BOOLEAN DEFAULT FALSE,
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_menu_items_category ON menu_items(category_id);
CREATE INDEX idx_menu_items_name ON menu_items(name);
CREATE INDEX idx_menu_items_availability ON menu_items(is_available);
CREATE INDEX idx_menu_items_food_type ON menu_items(food_type);