CREATE TABLE auth_actor (
    id BINARY(16) PRIMARY KEY UNIQUE,
    issuer TEXT NOT NULL,
    subject TEXT NOT NULL,
    full_name TEXT NOT NULL,
    email TEXT,
    disabled_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    last_seen_at TIMESTAMP NOT NULL,
    UNIQUE (issuer, subject)
);

CREATE TABLE auth_actor_role (
    auth_actor_id BINARY(16) NOT NULL,
    auth_role_id BINARY(16) NOT NULL,
    PRIMARY KEY (auth_actor_id, auth_role_id),
    FOREIGN KEY (auth_actor_id) REFERENCES auth_actor (id),
    FOREIGN KEY (auth_role_id) REFERENCES auth_role (id)
);

CREATE TABLE auth_client (
    client_id TEXT PRIMARY KEY,
    origin TEXT NOT NULL,
    original_registration_payload TEXT,
    redirect_uris_json TEXT NOT NULL,
    grant_types_json TEXT NOT NULL,
    response_types_json TEXT NOT NULL,
    token_endpoint_auth_method TEXT NOT NULL,
    client_name TEXT,
    client_uri TEXT,
    logo_uri TEXT,
    contacts_json TEXT NOT NULL,
    software_id TEXT,
    software_version TEXT,
    tos_uri TEXT,
    policy_uri TEXT,
    created_at TIMESTAMP NOT NULL,
    last_used_at TIMESTAMP NOT NULL
);

CREATE TABLE auth_code (
    code TEXT PRIMARY KEY,
    client_id TEXT NOT NULL,
    redirect_uri TEXT NOT NULL,
    subject TEXT NOT NULL,
    scope TEXT NOT NULL,
    code_challenge TEXT NOT NULL,
    code_challenge_method TEXT NOT NULL,
    nonce TEXT,
    auth_time TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL
);

CREATE TABLE auth_ctx (
    authorize_ctx_code TEXT PRIMARY KEY,
    client_id TEXT NOT NULL,
    redirect_uri TEXT NOT NULL,
    scope TEXT NOT NULL,
    state TEXT,
    code_challenge TEXT NOT NULL,
    code_challenge_method TEXT NOT NULL,
    nonce TEXT,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL
);

CREATE TABLE auth_role (
    id BINARY(16) PRIMARY KEY UNIQUE,
    key VARCHAR(30) NOT NULL,
    name VARCHAR(30) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL,
    last_updated_at TIMESTAMP NOT NULL
);

CREATE TABLE auth_role_permission (
    auth_role_id BINARY(16) NOT NULL,
    permission VARCHAR(50) NOT NULL,
    PRIMARY KEY (auth_role_id, permission),
    FOREIGN KEY (auth_role_id) REFERENCES auth_role (id)
);

CREATE TABLE users (
    id BINARY(16) PRIMARY KEY UNIQUE,
    login TEXT NOT NULL UNIQUE,
    full_name TEXT NOT NULL,
    password_hash TEXT NOT NULL,
    admin INTEGER NOT NULL,
    bootstrap INTEGER NOT NULL,
    disabled_date TIMESTAMP
);

CREATE INDEX idx_auth_actor_created_at ON auth_actor (created_at);
CREATE INDEX idx_auth_actor_issuer_subject ON auth_actor (issuer, subject);
CREATE INDEX idx_auth_code_expires_at ON auth_code (expires_at);
CREATE INDEX idx_auth_ctx_expires_at ON auth_ctx (expires_at);
CREATE UNIQUE INDEX idx_auth_role_key ON auth_role (key);

INSERT INTO auth_actor (id, issuer, subject, full_name, email, disabled_date, created_at, last_seen_at)
VALUES (
    X'01941F297C0070009A6567088EBCBABD',
    'urn:medatarun:system',
    'system-maintenance',
    'System maintenance',
    NULL,
    NULL,
    '2025-01-01 02:00:00.000',
    '2025-01-01 02:00:00.000'
);
