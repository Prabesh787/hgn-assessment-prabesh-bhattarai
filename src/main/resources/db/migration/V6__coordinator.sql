-- Coordinator: who acknowledges the alert and respond to it.

CREATE TABLE IF NOT EXISTS coordinator (
    id         BIGSERIAL PRIMARY KEY,
    name       TEXT        NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);


ALTER TABLE alert DROP CONSTRAINT IF EXISTS ck_alert_claim_consistent;

ALTER TABLE alert DROP COLUMN IF EXISTS claimed_by;

ALTER TABLE alert
    ADD COLUMN IF NOT EXISTS claimed_by_id BIGINT REFERENCES coordinator (id);

UPDATE alert
   SET claimed_at = NULL,
       status     = CASE WHEN status = 'CLAIMED' THEN 'OPEN' ELSE status END
 WHERE claimed_by_id IS NULL
   AND claimed_at IS NOT NULL;

ALTER TABLE alert
    ADD CONSTRAINT ck_alert_claim_consistent
        CHECK ((claimed_by_id IS NULL) = (claimed_at IS NULL));

CREATE INDEX IF NOT EXISTS ix_alert_claimed_by ON alert (claimed_by_id);
