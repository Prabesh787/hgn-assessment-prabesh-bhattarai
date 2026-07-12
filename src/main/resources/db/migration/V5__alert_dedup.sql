DROP INDEX IF EXISTS ux_device_active_alert;

ALTER TABLE alert
    ADD COLUMN IF NOT EXISTS retransmission_count INTEGER NOT NULL DEFAULT 0;

ALTER TABLE alert
    DROP CONSTRAINT IF EXISTS ck_alert_retransmission_count;

ALTER TABLE alert
    ADD CONSTRAINT ck_alert_retransmission_count CHECK (retransmission_count >= 0);

ALTER TABLE alert_signal
    ADD COLUMN IF NOT EXISTS kind VARCHAR(20) NOT NULL DEFAULT 'RAISED';

ALTER TABLE alert_signal
    DROP CONSTRAINT IF EXISTS ck_alert_signal_kind;

ALTER TABLE alert_signal
    ADD CONSTRAINT ck_alert_signal_kind CHECK (kind IN ('RAISED', 'RETRANSMISSION'));
