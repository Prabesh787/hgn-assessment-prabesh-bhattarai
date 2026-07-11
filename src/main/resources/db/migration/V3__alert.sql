CREATE TABLE alert (
    id             BIGSERIAL PRIMARY KEY,
    device_id      BIGINT      NOT NULL REFERENCES device (id),
    order_id       BIGINT      REFERENCES trek_order (id),
    status         VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    latitude       DOUBLE PRECISION,
    longitude      DOUBLE PRECISION,
    raised_at      TIMESTAMPTZ NOT NULL,
    last_signal_at TIMESTAMPTZ NOT NULL,
    signal_count   INTEGER     NOT NULL DEFAULT 1,
    claimed_by     TEXT,
    claimed_at     TIMESTAMPTZ,
    resolved_at    TIMESTAMPTZ,
    escalated_at   TIMESTAMPTZ,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT ck_alert_status
        CHECK (status IN ('OPEN', 'CLAIMED', 'ESCALATED', 'RESOLVED')),

    CONSTRAINT ck_alert_signal_count
        CHECK (signal_count >= 1),

    CONSTRAINT ck_alert_signal_window
        CHECK (last_signal_at >= raised_at),

    CONSTRAINT ck_alert_claim_consistent
        CHECK ((claimed_by IS NULL) = (claimed_at IS NULL)),

    CONSTRAINT ck_alert_resolved_consistent
        CHECK ((status = 'RESOLVED') = (resolved_at IS NOT NULL)),

    CONSTRAINT ck_alert_location
        CHECK ((latitude IS NULL) = (longitude IS NULL))
);

CREATE UNIQUE INDEX ux_device_active_alert
    ON alert (device_id)
    WHERE status <> 'RESOLVED';

CREATE INDEX ix_alert_status_raised ON alert (status, raised_at);

CREATE INDEX ix_alert_device ON alert (device_id, raised_at DESC);

CREATE INDEX ix_alert_order ON alert (order_id);
