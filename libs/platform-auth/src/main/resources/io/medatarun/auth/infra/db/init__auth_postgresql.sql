CREATE TABLE auth_actor (
    id UUID PRIMARY KEY,
    issuer TEXT NOT NULL,
    subject TEXT NOT NULL,
    full_name TEXT NOT NULL,
    email TEXT,
    disabled_date TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    last_seen_at TIMESTAMPTZ NOT NULL,
    UNIQUE (issuer, subject)
);

CREATE TABLE auth_role (
    id UUID PRIMARY KEY,
    key VARCHAR(30) NOT NULL,
    name VARCHAR(30) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    last_updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE auth_role_permission (
    auth_role_id UUID NOT NULL,
    permission VARCHAR(50) NOT NULL,
    PRIMARY KEY (auth_role_id, permission),
    FOREIGN KEY (auth_role_id) REFERENCES auth_role (id)
);

CREATE TABLE auth_actor_role (
    auth_actor_id UUID NOT NULL,
    auth_role_id UUID NOT NULL,
    PRIMARY KEY (auth_actor_id, auth_role_id),
    FOREIGN KEY (auth_actor_id) REFERENCES auth_actor (id),
    FOREIGN KEY (auth_role_id) REFERENCES auth_role (id)
);

CREATE TABLE auth_client (
    client_id TEXT PRIMARY KEY,
    origin TEXT NOT NULL,
    original_registration_payload JSONB,
    redirect_uris_json JSONB NOT NULL,
    grant_types_json JSONB NOT NULL,
    response_types_json JSONB NOT NULL,
    token_endpoint_auth_method TEXT NOT NULL,
    client_name TEXT,
    client_uri TEXT,
    logo_uri TEXT,
    contacts_json JSONB NOT NULL,
    software_id TEXT,
    software_version TEXT,
    tos_uri TEXT,
    policy_uri TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    last_used_at TIMESTAMPTZ NOT NULL
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
    auth_time TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL
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
    created_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE users (
    id UUID PRIMARY KEY,
    login TEXT NOT NULL UNIQUE,
    full_name TEXT NOT NULL,
    password_hash TEXT NOT NULL,
    admin BOOLEAN NOT NULL,
    bootstrap BOOLEAN NOT NULL,
    disabled_date TIMESTAMPTZ
);

CREATE INDEX idx_auth_actor_created_at ON auth_actor (created_at);
CREATE INDEX idx_auth_actor_issuer_subject ON auth_actor (issuer, subject);
CREATE UNIQUE INDEX idx_auth_role_key ON auth_role (key);
CREATE INDEX idx_auth_code_expires_at ON auth_code (expires_at);
CREATE INDEX idx_auth_ctx_expires_at ON auth_ctx (expires_at);

INSERT INTO auth_actor (id, issuer, subject, full_name, email, disabled_date, created_at, last_seen_at)
VALUES (
    '01941f29-7c00-7000-9a65-67088ebcbabd',
    'urn:medatarun:system',
    'system-maintenance',
    'System maintenance',
    NULL,
    NULL,
    '2025-01-01T00:00:00Z',
    '2025-01-01T00:00:00Z'
);
