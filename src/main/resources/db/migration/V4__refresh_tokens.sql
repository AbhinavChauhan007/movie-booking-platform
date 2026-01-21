CREATE TABLE refresh_tokens
(
    id       BIGSERIAL PRIMARY KEY,
    token    VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL,
    expiry   TIMESTAMP    NOT NULL,
    revoked  BOOLEAN      NOT NULL
);

CREATE TABLE refresh_token_roles
(
    refresh_token_id BIGINT       NOT NULL,
    role             VARCHAR(255) NOT NULL,

    CONSTRAINT fk_refresh_token_roles_token
        FOREIGN KEY (refresh_token_id)
            REFERENCES refresh_tokens (id)
            ON DELETE CASCADE
);

