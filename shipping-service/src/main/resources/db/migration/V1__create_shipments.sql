-- Create shipments table
CREATE TABLE shipments (
    id          UUID          PRIMARY KEY,
    order_id    UUID          NOT NULL,
    customer_id UUID          NOT NULL,
    status      VARCHAR(20)   NOT NULL,
    created_at  TIMESTAMPTZ   NOT NULL,
    updated_at  TIMESTAMPTZ   NOT NULL
);

CREATE UNIQUE INDEX idx_shipments_order_id ON shipments (order_id);
CREATE INDEX idx_shipments_customer_id ON shipments (customer_id);