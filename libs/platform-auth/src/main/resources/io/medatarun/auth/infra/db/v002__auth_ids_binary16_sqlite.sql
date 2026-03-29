PRAGMA foreign_keys = OFF;

DROP TABLE IF EXISTS users_v002;
CREATE TABLE users_v002 (
  id BINARY(16) PRIMARY KEY UNIQUE,
  login TEXT NOT NULL UNIQUE,
  full_name TEXT NOT NULL,
  password_hash TEXT NOT NULL,
  admin INTEGER NOT NULL,
  bootstrap INTEGER NOT NULL,
  disabled_date TEXT
);

INSERT INTO users_v002 (id, login, full_name, password_hash, admin, bootstrap, disabled_date)
SELECT
  unhex(replace(id, '-', '')),
  login,
  full_name,
  password_hash,
  admin,
  bootstrap,
  disabled_date
FROM users;

DROP TABLE users;
ALTER TABLE users_v002 RENAME TO users;

DROP TABLE IF EXISTS actors_v002;
CREATE TABLE actors_v002 (
  id BINARY(16) PRIMARY KEY UNIQUE,
  issuer TEXT NOT NULL,
  subject TEXT NOT NULL,
  full_name TEXT NOT NULL,
  email TEXT,
  roles_json TEXT NOT NULL,
  disabled_date TEXT,
  created_at TEXT NOT NULL,
  last_seen_at TEXT NOT NULL,
  UNIQUE(issuer, subject)
);

INSERT INTO actors_v002 (id, issuer, subject, full_name, email, roles_json, disabled_date, created_at, last_seen_at)
SELECT
  unhex(replace(id, '-', '')),
  issuer,
  subject,
  full_name,
  email,
  roles_json,
  disabled_date,
  created_at,
  last_seen_at
FROM actors;

DROP TABLE actors;
ALTER TABLE actors_v002 RENAME TO actors;

CREATE INDEX IF NOT EXISTS idx_actors_issuer_subject ON actors(issuer, subject);
CREATE INDEX IF NOT EXISTS idx_actors_created_at ON actors(created_at);

PRAGMA foreign_keys = ON;
