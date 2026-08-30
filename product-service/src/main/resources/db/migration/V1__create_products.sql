-- Create products table
CREATE TABLE products (
    id          UUID          PRIMARY KEY,
    sku         VARCHAR(50)   NOT NULL,
    name        VARCHAR(100)  NOT NULL,
    description VARCHAR(1000) NULL,
    amount      NUMERIC(12,2) NOT NULL,
    currency    VARCHAR(3)    NOT NULL,
    active      BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ   NOT NULL
);

CREATE UNIQUE INDEX idx_products_sku ON products (sku);
