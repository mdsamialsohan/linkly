CREATE TABLE url (
                     id            BIGSERIAL PRIMARY KEY,
                     short_code    VARCHAR(16)  NOT NULL,
                     long_url      VARCHAR(2048) NOT NULL,
                     created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                     expires_at    TIMESTAMPTZ,
                     click_count   BIGINT       NOT NULL DEFAULT 0,
                     CONSTRAINT uq_url_short_code UNIQUE (short_code)
);
