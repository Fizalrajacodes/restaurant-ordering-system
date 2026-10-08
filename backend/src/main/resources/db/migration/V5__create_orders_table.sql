CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    table_id BIGINT NOT NULL REFERENCES restaurant_tables(id),
    customer_name VARCHAR(100) NOT NULL,
    customer_mobile VARCHAR(20) NOT NULL,
    customer_email VARCHAR(150),
    subtotal DECIMAL(10,2) NOT NULL,
    tax DECIMAL(10,2) NOT NULL,
    discount DECIMAL(10,2) DEFAULT 0,
    total_amount DECIMAL(10,2) NOT NULL,
    order_status VARCHAR(20) NOT NULL CHECK (order_status IN ('PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'SERVED', 'CANCELLED')),
    payment_status VARCHAR(20) NOT NULL CHECK (payment_status IN ('PAYMENT_PENDING', 'PAYMENT_COMPLETED', 'PAYMENT_FAILED', 'REFUNDED', 'PARTIAL_REFUND')),
    special_instructions VARCHAR(1000),
    estimated_ready_time TIMESTAMP,
    confirmed_at TIMESTAMP,
    preparing_at TIMESTAMP,
    ready_at TIMESTAMP,
    served_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    cancelled_reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_orders_number ON orders(order_number);
CREATE INDEX idx_orders_table ON orders(table_id);
CREATE INDEX idx_orders_status ON orders(order_status);
CREATE INDEX idx_orders_payment_status ON orders(payment_status);
CREATE INDEX idx_orders_customer_mobile ON orders(customer_mobile);
CREATE INDEX idx_orders_created_at ON orders(created_at);