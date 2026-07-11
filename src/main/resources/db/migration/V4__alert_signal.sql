CREATE TABLE alert_signal (
    id          BIGSERIAL PRIMARY KEY,
    alert_id    BIGINT      NOT NULL REFERENCES alert (id),
    latitude    DOUBLE PRECISION,
    longitude   DOUBLE PRECISION,
    signaled_at TIMESTAMPTZ NOT NULL,
    received_at TIMESTAMPTZ NOT NULL,
    seq         INTEGER     NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT ck_alert_signal_seq
        CHECK (seq >= 1),

    CONSTRAINT ck_alert_signal_location
        CHECK ((latitude IS NULL) = (longitude IS NULL))
);

CREATE UNIQUE INDEX ux_alert_signal_seq ON alert_signal (alert_id, seq);

CREATE INDEX ix_alert_signal_alert ON alert_signal (alert_id, signaled_at);
