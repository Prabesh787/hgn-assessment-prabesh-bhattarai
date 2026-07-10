ALTER TABLE device_assignment
    ADD COLUMN end_reason VARCHAR(20);

-- Every window closed so far was closed by a reassignment (the only path that
-- stamps assigned_to today), so backfill existing closed rows accordingly.
UPDATE device_assignment
SET end_reason = 'REASSIGNED'
WHERE active = FALSE
  AND end_reason IS NULL;

-- A window carries a reason exactly when it is closed: active <=> no reason.
ALTER TABLE device_assignment
    ADD CONSTRAINT ck_assignment_end_reason
        CHECK (active = (end_reason IS NULL));
