CREATE TABLE qr_codes (
    id BIGSERIAL PRIMARY KEY,
    table_id BIGINT NOT NULL UNIQUE REFERENCES restaurant_tables(id) ON DELETE CASCADE,
    token VARCHAR(100) NOT NULL UNIQUE,
    qr_data TEXT NOT NULL,
    image_url VARCHAR(500),
    image_data BYTEA,
    is_active BOOLEAN DEFAULT TRUE,
    scan_count INTEGER DEFAULT 0,
    last_scanned_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_qr_codes_table ON qr_codes(table_id);
CREATE INDEX idx_qr_codes_token ON qr_codes(token);