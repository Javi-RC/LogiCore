-- Create processed_events table for at-least-once consumer idempotency.
CREATE TABLE processed_events (
    event_id     UUID        PRIMARY KEY,
    processed_at TIMESTAMPTZ NOT NULL
);