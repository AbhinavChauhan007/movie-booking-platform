-- Migration: Add total_price column to bookings table
ALTER TABLE bookings ADD COLUMN total_price DOUBLE PRECISION;

-- Add check constraint
ALTER TABLE bookings
  ADD CONSTRAINT chk_total_price_non_negative
  CHECK (total_price IS NULL OR total_price >= 0);