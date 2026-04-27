CREATE TABLE auth_refresh_token (
    id BINARY(16) PRIMARY KEY UNIQUE,
    token_hash TEXT NOT NULL UNIQUE,
    client_id TEXT NOT NULL,
    subject TEXT NOT NULL,
    scope TEXT NOT NULL,
    auth_time INTEGER NOT NULL,
    expires_at INTEGER NOT NULL,
    revoked_at INTEGER,
    replaced_by_id BINARY(16),
    nonce TEXT,
    FOREIGN KEY (replaced_by_id) REFERENCES auth_refresh_token (id)
);

CREATE INDEX idx_auth_refresh_token_expires_at ON auth_refresh_token (expires_at);
CREATE INDEX idx_auth_refresh_token_token_hash ON auth_refresh_token (token_hash);
