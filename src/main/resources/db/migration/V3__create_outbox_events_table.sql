CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    next_attempt_at TIMESTAMP WITH TIME ZONE NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE NULL,
    last_error VARCHAR(1000) NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_outbox_events_status
        CHECK (status IN ('PENDING', 'RETRY', 'PUBLISHED', 'DEAD')),
    CONSTRAINT chk_outbox_events_attempts CHECK (attempts >= 0)
);

CREATE INDEX idx_outbox_events_ready
    ON outbox_events (status, next_attempt_at, created_at);
