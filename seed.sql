INSERT INTO url (short_code, long_url, created_at, click_count)
SELECT
    lpad(i::text, 7, '0'),
    'https://example.com/page/' || i,
    NOW() - (random() * interval '365 days'),
    floor(random() * 100000)::bigint
FROM generate_series(1, 1000000) AS s(i);
