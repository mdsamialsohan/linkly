CREATE TABLE click_hourly (
                              short_code   VARCHAR(16)  NOT NULL,
                              bucket_hour  TIMESTAMPTZ  NOT NULL,
                              click_count  BIGINT       NOT NULL DEFAULT 0,
                              PRIMARY KEY (short_code, bucket_hour)
);