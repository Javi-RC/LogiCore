-- Create notifications table
CREATE TABLE notifications (
    id             UUID          PRIMARY KEY,
    type           VARCHAR(30)   NOT NULL,
    correlation_id UUID          NOT NULL,
    recipient      VARCHAR(255)  NOT NULL,
    message        VARCHAR(1000) NOT NULL,
    created_at     TIMESTAMPTZ   NOT NULL
);

CREATE INDEX idx_notifications_correlation_id ON notifications (correlation_id);