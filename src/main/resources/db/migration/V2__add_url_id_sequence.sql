-- Dedicated sequence for short code generation.
-- Separated from the row id sequence so we can allocate codes
-- without inserting rows (useful for reservations, tests, etc.).
CREATE SEQUENCE url_code_seq START WITH 100000 INCREMENT BY 1;