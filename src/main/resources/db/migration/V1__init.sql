-- HGN SOS alert intake & dispatch -- baseline schema.


-- device

CREATE TABLE device (
    id            BIGSERIAL PRIMARY KEY,
    device_serial TEXT        NOT NULL UNIQUE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

--trek_order:

CREATE TABLE trek_order (
    id         BIGSERIAL PRIMARY KEY,
    order_ref  TEXT        NOT NULL UNIQUE,
    trek_name  TEXT        NOT NULL,
    starts_at  TIMESTAMPTZ,
    ends_at    TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT ck_trek_order_window
        CHECK (ends_at IS NULL OR starts_at IS NULL OR ends_at > starts_at)
);

CREATE TABLE trekker (
    id         BIGSERIAL PRIMARY KEY,
    order_id   BIGINT      NOT NULL REFERENCES trek_order (id),
    full_name  TEXT        NOT NULL,
    phone      TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX ix_trekker_order ON trekker (order_id);

CREATE TABLE device_assignment (
    id            BIGSERIAL PRIMARY KEY,
    device_id     BIGINT      NOT NULL REFERENCES device (id),
    order_id      BIGINT      NOT NULL REFERENCES trek_order (id),
    assigned_from TIMESTAMPTZ NOT NULL DEFAULT now(),
    assigned_to   TIMESTAMPTZ,
    active        BOOLEAN     NOT NULL DEFAULT TRUE,

    CONSTRAINT ck_assignment_window
        CHECK (assigned_to IS NULL OR assigned_to > assigned_from),

    CONSTRAINT ck_assignment_active_consistent
        CHECK (active = (assigned_to IS NULL))
);

CREATE UNIQUE INDEX ux_device_active_assignment
    ON device_assignment (device_id)
    WHERE active;

CREATE INDEX ix_assignment_device_window
    ON device_assignment (device_id, assigned_from DESC);

CREATE INDEX ix_assignment_order ON device_assignment (order_id);
