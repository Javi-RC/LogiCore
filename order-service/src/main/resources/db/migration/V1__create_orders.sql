-- Create orders table
CREATE TABLE orders (
    id          UUID          PRIMARY KEY,
    customer_id UUID          NOT NULL,
    amount      NUMERIC(12,2) NOT NULL,
    currency    VARCHAR(3)    NOT NULL,
    status      VARCHAR(20)   NOT NULL,
    created_at  TIMESTAMPTZ   NOT NULL,
    updated_at  TIMESTAMPTZ   NOT NULL
);

CREATE INDEX idx_orders_customer_id ON orders (customer_id);