CREATE TABLE click (
                       id          BIGSERIAL PRIMARY KEY,
                       short_code  VARCHAR(16)   NOT NULL,
                       clicked_at  TIMESTAMPTZ   NOT NULL,
                       referrer    VARCHAR(2048),
                       user_agent  VARCHAR(512)
);

CREATE INDEX idx_click_code_time ON click (short_code, clicked_at);