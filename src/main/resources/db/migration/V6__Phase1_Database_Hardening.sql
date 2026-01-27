-- ===============================================
-- V6__Phase1_Database_Hardening.sql
-- Phase 1: Production Readiness Enhancements
-- ===============================================

-- 1. Add missing foreign key constraints
ALTER TABLE seat_booking
    ADD CONSTRAINT fk_seat_booking_booking_id
        FOREIGN KEY (booking_id) REFERENCES bookings (booking_id) ON DELETE CASCADE;

ALTER TABLE booking_idempotency
    ADD CONSTRAINT fk_booking_idempotency_booking_id
        FOREIGN KEY (booking_id) REFERENCES bookings (booking_id) ON DELETE CASCADE;

-- 2. Add performance indexes for common queries
CREATE INDEX IF NOT EXISTS idx_booking_created_at
    ON bookings (created_at);

CREATE INDEX IF NOT EXISTS idx_seat_booking_show_id
    ON seat_booking (show_id);

CREATE INDEX IF NOT EXISTS idx_seat_booking_booking_id
    ON seat_booking (booking_id);

-- 3. Add optimistic locking support (your entities already have version)
-- Ensure version column has default value
ALTER TABLE bookings
    ALTER COLUMN version SET DEFAULT 0;

-- 4. Add audit fields for better debugging
ALTER TABLE bookings
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100);

-- 5. Enhance booking_idempotency for cleanup jobs
ALTER TABLE booking_idempotency
    ALTER COLUMN created_date SET DEFAULT CURRENT_TIMESTAMP;

-- 6. Add composite indexes for complex queries
CREATE INDEX IF NOT EXISTS idx_bookings_status_created_updated
    ON bookings (booking_status, created_at, updated_at);

-- 7. Add check constraints for data integrity
ALTER TABLE seat_booking
    ADD CONSTRAINT chk_seat_number_format
        CHECK (seat_number ~ '^[A-Z][0-9]+$');

-- 8. Create partial indexes for performance
CREATE INDEX IF NOT EXISTS idx_bookings_active_status
    ON bookings (created_at)
    WHERE booking_status IN ('CREATED', 'INITIATED');

-- 9. Add table comments for documentation
COMMENT ON TABLE bookings IS 'Core booking entity with state transitions';
COMMENT ON TABLE booking_idempotency IS 'Ensures idempotent booking operations';
COMMENT ON TABLE seat_booking IS 'Maps bookings to specific seats for shows';

-- 10. Add trigger for automatic updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_bookings_updated_at
    BEFORE UPDATE ON bookings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();