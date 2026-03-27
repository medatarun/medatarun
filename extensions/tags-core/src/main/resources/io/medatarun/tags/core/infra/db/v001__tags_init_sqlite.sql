CREATE TABLE IF NOT EXISTS tag_group (
  id TEXT PRIMARY KEY UNIQUE,
  key TEXT NOT NULL UNIQUE,
  name TEXT,
  description TEXT
);

CREATE TABLE IF NOT EXISTS tag (
  id TEXT PRIMARY KEY UNIQUE,
  scope_type TEXT NOT NULL,
  scope_id TEXT,
  tag_group_id TEXT,
  key TEXT NOT NULL,
  name TEXT,
  description TEXT,
  FOREIGN KEY (tag_group_id) REFERENCES tag_group(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_tag_scope_key
ON tag(scope_type, scope_id, key);

CREATE INDEX IF NOT EXISTS idx_tag_group_key
ON tag(tag_group_id, key);
