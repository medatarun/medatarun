CREATE TABLE auth_actor (
    id uuid NOT NULL,
    issuer text NOT NULL,
    subject text NOT NULL,
    full_name text NOT NULL,
    email text,
    disabled_date timestamp with time zone,
    created_at timestamp with time zone NOT NULL,
    last_seen_at timestamp with time zone NOT NULL
);

CREATE TABLE auth_actor_role (
    auth_actor_id uuid NOT NULL,
    auth_role_id uuid NOT NULL
);

CREATE TABLE auth_client (
    client_id text NOT NULL,
    origin text NOT NULL,
    original_registration_payload jsonb,
    redirect_uris_json jsonb NOT NULL,
    grant_types_json jsonb NOT NULL,
    response_types_json jsonb NOT NULL,
    token_endpoint_auth_method text NOT NULL,
    client_name text,
    client_uri text,
    logo_uri text,
    contacts_json jsonb NOT NULL,
    software_id text,
    software_version text,
    tos_uri text,
    policy_uri text,
    created_at timestamp with time zone NOT NULL,
    last_used_at timestamp with time zone NOT NULL
);

CREATE TABLE auth_code (
    code text NOT NULL,
    client_id text NOT NULL,
    redirect_uri text NOT NULL,
    subject text NOT NULL,
    scope text NOT NULL,
    code_challenge text NOT NULL,
    code_challenge_method text NOT NULL,
    nonce text,
    auth_time timestamp with time zone NOT NULL,
    expires_at timestamp with time zone NOT NULL
);

CREATE TABLE auth_ctx (
    authorize_ctx_code text NOT NULL,
    client_id text NOT NULL,
    redirect_uri text NOT NULL,
    scope text NOT NULL,
    state text,
    code_challenge text NOT NULL,
    code_challenge_method text NOT NULL,
    nonce text,
    created_at timestamp with time zone NOT NULL,
    expires_at timestamp with time zone NOT NULL
);

CREATE TABLE auth_refresh_token (
    id uuid NOT NULL,
    token_hash text NOT NULL,
    client_id text NOT NULL,
    subject text NOT NULL,
    scope text NOT NULL,
    auth_time timestamp with time zone NOT NULL,
    expires_at timestamp with time zone NOT NULL,
    revoked_at timestamp with time zone,
    replaced_by_id uuid,
    nonce text
);

CREATE TABLE auth_role (
    id uuid NOT NULL,
    key character varying(30) NOT NULL,
    name character varying(30) NOT NULL,
    description text,
    created_at timestamp with time zone NOT NULL,
    last_updated_at timestamp with time zone NOT NULL,
    auto_assign boolean DEFAULT false NOT NULL
);

CREATE TABLE auth_role_permission (
    auth_role_id uuid NOT NULL,
    permission character varying(50) NOT NULL
);

CREATE TABLE users (
    id uuid NOT NULL,
    login text NOT NULL,
    full_name text NOT NULL,
    password_hash text NOT NULL,
    admin boolean NOT NULL,
    bootstrap boolean NOT NULL,
    disabled_date timestamp with time zone
);

ALTER TABLE ONLY auth_actor
ADD CONSTRAINT auth_actor_issuer_subject_key UNIQUE (issuer, subject);

ALTER TABLE ONLY auth_actor
ADD CONSTRAINT auth_actor_pkey PRIMARY KEY (id);

ALTER TABLE ONLY auth_actor_role
ADD CONSTRAINT auth_actor_role_pkey PRIMARY KEY (auth_actor_id, auth_role_id);

ALTER TABLE ONLY auth_client
ADD CONSTRAINT auth_client_pkey PRIMARY KEY (client_id);

ALTER TABLE ONLY auth_code
ADD CONSTRAINT auth_code_pkey PRIMARY KEY (code);

ALTER TABLE ONLY auth_ctx
ADD CONSTRAINT auth_ctx_pkey PRIMARY KEY (authorize_ctx_code);

ALTER TABLE ONLY auth_refresh_token
ADD CONSTRAINT auth_refresh_token_pkey PRIMARY KEY (id);

ALTER TABLE ONLY auth_refresh_token
ADD CONSTRAINT auth_refresh_token_token_hash_key UNIQUE (token_hash);

ALTER TABLE ONLY auth_role_permission
ADD CONSTRAINT auth_role_permission_pkey PRIMARY KEY (auth_role_id, permission);

ALTER TABLE ONLY auth_role
ADD CONSTRAINT auth_role_pkey PRIMARY KEY (id);

ALTER TABLE ONLY users
ADD CONSTRAINT users_login_key UNIQUE (login);

ALTER TABLE ONLY users
ADD CONSTRAINT users_pkey PRIMARY KEY (id);

CREATE INDEX idx_auth_actor_created_at ON auth_actor USING btree (created_at);

CREATE INDEX idx_auth_actor_issuer_subject ON auth_actor USING btree (issuer, subject);

CREATE INDEX idx_auth_code_expires_at ON auth_code USING btree (expires_at);

CREATE INDEX idx_auth_ctx_expires_at ON auth_ctx USING btree (expires_at);

CREATE INDEX idx_auth_refresh_token_expires_at ON auth_refresh_token USING btree (expires_at);

CREATE INDEX idx_auth_refresh_token_token_hash ON auth_refresh_token USING btree (token_hash);

CREATE UNIQUE INDEX idx_auth_role_auto_assign ON auth_role USING btree (auto_assign) WHERE (auto_assign IS true);

CREATE UNIQUE INDEX idx_auth_role_key ON auth_role USING btree (key);

ALTER TABLE ONLY auth_actor_role
ADD CONSTRAINT auth_actor_role_auth_actor_id_fkey FOREIGN KEY (auth_actor_id) REFERENCES auth_actor (id);

ALTER TABLE ONLY auth_actor_role
ADD CONSTRAINT auth_actor_role_auth_role_id_fkey FOREIGN KEY (auth_role_id) REFERENCES auth_role (id);

ALTER TABLE ONLY auth_refresh_token
ADD CONSTRAINT auth_refresh_token_replaced_by_id_fkey FOREIGN KEY (replaced_by_id) REFERENCES auth_refresh_token (id);

ALTER TABLE ONLY auth_role_permission
ADD CONSTRAINT auth_role_permission_auth_role_id_fkey FOREIGN KEY (auth_role_id) REFERENCES auth_role (id);

INSERT INTO auth_actor (id, issuer, subject, full_name, email, disabled_date, created_at, last_seen_at)
VALUES (
    '01941f29-7c00-7000-9a65-67088ebcbabd',
    'urn:medatarun:system',
    'system-maintenance',
    'System maintenance',
    null,
    null,
    '2025-01-01T00:00:00Z',
    '2025-01-01T00:00:00Z'
);
