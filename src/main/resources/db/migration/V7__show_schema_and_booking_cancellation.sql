-- V7__show_schema.sql

CREATE TABLE show (
    id BIGSERIAL PRIMARY KEY,
    movie_id BIGINT NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    screen_number INT NOT NULL,
    total_seats INT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_show_movie
        FOREIGN KEY (movie_id)
        REFERENCES movie(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_show_movie_id ON show(movie_id);
CREATE INDEX idx_show_start_time ON show(start_time);


ALTER TABLE bookings
ADD COLUMN cancellation_reason VARCHAR(32);

CREATE INDEX idx_bookings_cancellation_reason
ON bookings (cancellation_reason);

ALTER TABLE bookings
ADD CONSTRAINT chk_cancellation_reason_required
CHECK (
    (booking_status IN ('CANCELLED', 'EXPIRED') AND cancellation_reason IS NOT NULL)
    OR
    (booking_status NOT IN ('CANCELLED', 'EXPIRED') AND cancellation_reason IS NULL)
);


