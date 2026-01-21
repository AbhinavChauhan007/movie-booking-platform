CREATE TABLE movie
(
    id             BIGSERIAL PRIMARY KEY,
    title          VARCHAR(255) NOT NULL,
    genre          VARCHAR(255) NOT NULL,
    duration_mins  INTEGER      NOT NULL
);


CREATE TABLE seat_booking
(
    id          BIGSERIAL PRIMARY KEY,
    booking_id  BIGINT       NOT NULL,
    show_id     BIGINT       NOT NULL,
    seat_number VARCHAR(50)  NOT NULL,

    CONSTRAINT uk_show_seat UNIQUE (show_id, seat_number)
);
