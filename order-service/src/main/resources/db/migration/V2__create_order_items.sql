-- Create order_items table (children of orders)
CREATE TABLE order_items (
    id          UUID          PRIMARY KEY,
    order_id    UUID          NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id  UUID          NOT NULL,
    quantity    INTEGER       NOT NULL CHECK (quantity > 0),
    amount      NUMERIC(12,2) NOT NULL,
    currency    VARCHAR(3)    NOT NULL
);

CREATE INDEX idx_order_items_order_id ON order_items (order_id);