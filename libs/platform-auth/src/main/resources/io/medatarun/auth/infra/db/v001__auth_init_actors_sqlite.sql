CREATE TABLE IF NOT EXISTS actors (
  id TEXT PRIMARY KEY UNIQUE,
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

CREATE INDEX IF NOT EXISTS idx_actors_issuer_subject ON actors(issuer, subject);
CREATE INDEX IF NOT EXISTS idx_actors_created_at ON actors(created_at);
