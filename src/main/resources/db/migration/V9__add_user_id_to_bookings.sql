-- Migration: Add user_id column to bookings table
ALTER TABLE bookings ADD COLUMN user_id BIGINT NOT NULL;

-- Add foreign key constraint
ALTER TABLE bookings
    ADD CONSTRAINT fk_bookings_user
    FOREIGN KEY (user_id) REFERENCES app_user(id);

-- Add index for performance
CREATE INDEX idx_bookings_user_id ON bookings(user_id);