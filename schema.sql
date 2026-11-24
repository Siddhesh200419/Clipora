-- Run this once in your PostgreSQL database before starting the app.
CREATE TABLE IF NOT EXISTS clipboards (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(6) NOT NULL UNIQUE,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMPTZ NOT NULL DEFAULT (CURRENT_TIMESTAMP + INTERVAL '15 minutes')
);

CREATE INDEX IF NOT EXISTS clipboards_expires_at_idx ON clipboards (expires_at);
