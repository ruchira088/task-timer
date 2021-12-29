
CREATE TABLE users (
    id VARCHAR(36),
    created_at TIMESTAMP NOT NULL,
    first_name VARCHAR(127) NOT NULL,
    last_name VARCHAR(255) NULL,
    email VARCHAR(127) UNIQUE NOT NULL,
    PRIMARY KEY (id)
);