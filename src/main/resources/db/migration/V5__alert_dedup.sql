DROP INDEX ux_device_active_alert;

ALTER TABLE alert
    ADD COLUMN retransmission_count INTEGER NOT NULL DEFAULT 0;

ALTER TABLE alert
    ADD CONSTRAINT ck_alert_retransmission_count CHECK (retransmission_count >= 0);

ALTER TABLE alert_signal
    ADD COLUMN kind VARCHAR(20) NOT NULL DEFAULT 'RAISED';

ALTER TABLE alert_signal
    ADD CONSTRAINT ck_alert_signal_kind CHECK (kind IN ('RAISED', 'RETRANSMISSION'));
