CREATE TABLE booking_idempotency
(
    idempotency_key VARCHAR(128) NOT NULL,
    booking_id      BIGINT       NOT NULL,
    created_date    TIMESTAMP    NOT NULL,

    CONSTRAINT pk_booking_idempotency PRIMARY KEY (idempotency_key)
);

CREATE INDEX idx_booking_idempotency_created_date
    ON booking_idempotency (created_date);
