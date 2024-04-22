-- 1.0 Create servers and state tables
CREATE TABLE states
(
    id      SERIAL PRIMARY KEY,
    city    VARCHAR(255) NOT NULL,
    country VARCHAR(255) NOT NULL
);

CREATE TABLE servers
(
    id        SERIAL PRIMARY KEY,
    bandwidth INTEGER     NOT NULL,
    latency   INTEGER     NOT NULL,
    purpose   VARCHAR(20) NOT NULL,
    state_id  INTEGER
        CONSTRAINT fk_servers_state REFERENCES states
);

CREATE INDEX idx_servers_state_id ON servers (state_id);
