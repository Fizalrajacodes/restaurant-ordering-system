CREATE TABLE restaurant_tables (
    id BIGSERIAL PRIMARY KEY,
    table_number VARCHAR(20) NOT NULL UNIQUE,
    table_name VARCHAR(50) NOT NULL,
    capacity INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('AVAILABLE', 'OCCUPIED', 'RESERVED', 'MAINTENANCE', 'INACTIVE')),
    qr_token VARCHAR(100) UNIQUE,
    qr_code_url VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tables_number ON restaurant_tables(table_number);
CREATE INDEX idx_tables_status ON restaurant_tables(status);
CREATE INDEX idx_tables_qr_token ON restaurant_tables(qr_token);