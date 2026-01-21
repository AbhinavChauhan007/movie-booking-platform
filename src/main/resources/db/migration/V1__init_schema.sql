CREATE TABLE bookings
(
    booking_id     BIGSERIAL PRIMARY KEY,

    booking_status VARCHAR(32)              NOT NULL,

    created_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at     TIMESTAMP WITH TIME ZONE NOT NULL,

    version        BIGINT                   NOT NULL
);

-- Fast lookup by booking id (already covered by PK)
-- Index for expiry reconciliation
CREATE INDEX idx_booking_status_created_at
    ON bookings (booking_status, created_at);

-- Safety check
ALTER TABLE bookings
    ADD CONSTRAINT chk_booking_status
        CHECK (booking_status in (
              'CREATED',
              'INITIATED',
              'CONFIRMED',
              'CANCELLED',
              'EXPIRED'
            ));