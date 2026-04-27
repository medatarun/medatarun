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

ALTER TABLE ONLY auth_refresh_token
ADD CONSTRAINT auth_refresh_token_pkey PRIMARY KEY (id);

ALTER TABLE ONLY auth_refresh_token
ADD CONSTRAINT auth_refresh_token_token_hash_key UNIQUE (token_hash);

ALTER TABLE ONLY auth_refresh_token
ADD CONSTRAINT auth_refresh_token_replaced_by_id_fkey FOREIGN KEY (replaced_by_id) REFERENCES auth_refresh_token (id);

CREATE INDEX idx_auth_refresh_token_expires_at ON auth_refresh_token USING btree (expires_at);
CREATE INDEX idx_auth_refresh_token_token_hash ON auth_refresh_token USING btree (token_hash);
