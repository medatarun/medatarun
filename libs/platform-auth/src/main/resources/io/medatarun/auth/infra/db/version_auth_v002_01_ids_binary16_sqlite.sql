PRAGMA foreign_keys = OFF;

DROP TABLE IF EXISTS users_v002;
CREATE TABLE users_v002 (
  id BINARY(16) PRIMARY KEY UNIQUE,
  login TEXT NOT NULL UNIQUE,
  full_name TEXT NOT NULL,
  password_hash TEXT NOT NULL,
  admin INTEGER NOT NULL,
  bootstrap INTEGER NOT NULL,
  disabled_date INTEGER
);

INSERT INTO users_v002 (id, login, full_name, password_hash, admin, bootstrap, disabled_date)
SELECT
  unhex(replace(id, '-', '')),
  login,
  full_name,
  password_hash,
  admin,
  bootstrap,
  CASE
    WHEN disabled_date IS NULL THEN NULL
    ELSE CAST(unixepoch(disabled_date, 'subsec') * 1000 AS INTEGER)
  END
FROM users;

DROP TABLE users;
ALTER TABLE users_v002 RENAME TO users;

DROP TABLE IF EXISTS auth_actor_v002;
CREATE TABLE auth_actor_v002 (
  id BINARY(16) PRIMARY KEY UNIQUE,
  issuer TEXT NOT NULL,
  subject TEXT NOT NULL,
  full_name TEXT NOT NULL,
  email TEXT,
  roles_json TEXT NOT NULL,
  disabled_date INTEGER,
  created_at INTEGER NOT NULL,
  last_seen_at INTEGER NOT NULL,
  UNIQUE(issuer, subject)
);

INSERT INTO auth_actor_v002 (id, issuer, subject, full_name, email, roles_json, disabled_date, created_at, last_seen_at)
SELECT
  unhex(replace(id, '-', '')),
  issuer,
  subject,
  full_name,
  email,
  roles_json,
  CASE
    WHEN disabled_date IS NULL THEN NULL
    ELSE CAST(unixepoch(disabled_date, 'subsec') * 1000 AS INTEGER)
  END,
  CAST(unixepoch(created_at, 'subsec') * 1000 AS INTEGER),
  CAST(unixepoch(last_seen_at, 'subsec') * 1000 AS INTEGER)
FROM actors;

DROP TABLE actors;
ALTER TABLE auth_actor_v002 RENAME TO auth_actor;

DROP TABLE IF EXISTS auth_ctx_v002;
CREATE TABLE auth_ctx_v002 (
    authorize_ctx_code TEXT PRIMARY KEY,
    client_id TEXT NOT NULL,
    redirect_uri TEXT NOT NULL,
    scope TEXT NOT NULL,
    state TEXT,
    code_challenge TEXT NOT NULL,
    code_challenge_method TEXT NOT NULL,
    nonce TEXT,
    created_at INTEGER NOT NULL,
    expires_at INTEGER NOT NULL
);

INSERT INTO auth_ctx_v002 (authorize_ctx_code, client_id, redirect_uri, scope, state, code_challenge, code_challenge_method, nonce, created_at, expires_at)
SELECT
  authorize_ctx_code,
  client_id,
  redirect_uri,
  scope,
  state,
  code_challenge,
  code_challenge_method,
  nonce,
  CAST(unixepoch(created_at, 'subsec') * 1000 AS INTEGER),
  CAST(unixepoch(expires_at, 'subsec') * 1000 AS INTEGER)
FROM auth_ctx;

DROP TABLE auth_ctx;
ALTER TABLE auth_ctx_v002 RENAME TO auth_ctx;

DROP TABLE IF EXISTS auth_code_v002;
CREATE TABLE auth_code_v002 (
    code TEXT PRIMARY KEY,
    client_id TEXT NOT NULL,
    redirect_uri TEXT NOT NULL,
    subject TEXT NOT NULL,
    scope TEXT NOT NULL,
    code_challenge TEXT NOT NULL,
    code_challenge_method TEXT NOT NULL,
    nonce TEXT,
    auth_time INTEGER NOT NULL,
    expires_at INTEGER NOT NULL
);

INSERT INTO auth_code_v002 (code, client_id, redirect_uri, subject, scope, code_challenge, code_challenge_method, nonce, auth_time, expires_at)
SELECT
  code,
  client_id,
  redirect_uri,
  subject,
  scope,
  code_challenge,
  code_challenge_method,
  nonce,
  CAST(unixepoch(auth_time, 'subsec') * 1000 AS INTEGER),
  CAST(unixepoch(expires_at, 'subsec') * 1000 AS INTEGER)
FROM auth_code;

DROP TABLE auth_code;
ALTER TABLE auth_code_v002 RENAME TO auth_code;

CREATE INDEX IF NOT EXISTS idx_auth_actor_issuer_subject ON auth_actor(issuer, subject);
CREATE INDEX IF NOT EXISTS idx_auth_actor_created_at ON auth_actor(created_at);
CREATE INDEX IF NOT EXISTS idx_auth_code_expires_at ON auth_code(expires_at);
CREATE INDEX IF NOT EXISTS idx_auth_ctx_expires_at ON auth_ctx(expires_at);

PRAGMA foreign_keys = ON;
