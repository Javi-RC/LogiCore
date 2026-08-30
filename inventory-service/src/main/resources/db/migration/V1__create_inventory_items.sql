-- Create inventory_items table
-- The version column is used by Hibernate @Version for optimistic locking.
CREATE TABLE inventory_items (
    product_id         UUID          PRIMARY KEY,
    available_quantity INTEGER       NOT NULL CHECK (available_quantity >= 0),
    reserved_quantity  INTEGER       NOT NULL CHECK (reserved_quantity >= 0),
    version            BIGINT        NOT NULL DEFAULT 0,
    created_at         TIMESTAMPTZ   NOT NULL,
    updated_at         TIMESTAMPTZ   NOT NULL
);