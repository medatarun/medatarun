CREATE TABLE IF NOT EXISTS users (
  id TEXT PRIMARY KEY UNIQUE,
  login TEXT NOT NULL UNIQUE,
  full_name TEXT NOT NULL,
  password_hash TEXT NOT NULL,
  admin INTEGER NOT NULL,
  bootstrap INTEGER NOT NULL,
  disabled_date TEXT
);
