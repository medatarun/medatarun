CREATE TABLE IF NOT EXISTS auth_client (
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
    created_at INTEGER NOT NULL,
    last_used_at INTEGER NOT NULL
);
