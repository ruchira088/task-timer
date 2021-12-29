
CREATE TABLE credentials (
    user_id VARCHAR(36),
    created_at TIMESTAMP NOT NULL,
    hashed_password VARCHAR(127) NOT NULL,
    PRIMARY KEY (user_id),
    CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES users(id)
);