CREATE TABLE IF NOT EXISTS process_history (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    process_id      BIGINT NOT NULL,
    status          varchar NOT NULL,
    created_at      timestamp NOT NULL DEFAULT NOW(),
    FOREIGN KEY (process_id) REFERENCES process(id)
)