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

CREATE TABLE IF NOT EXISTS tag_event
(
    id                  TEXT PRIMARY KEY UNIQUE,
    scope_type          TEXT    NOT NULL,
    scope_id            TEXT,
    stream_revision     INTEGER NOT NULL,
    event_type          TEXT    NOT NULL,
    event_version       INTEGER NOT NULL,
    actor_id            TEXT    NOT NULL,
    traceability_origin TEXT    NOT NULL,
    created_at          TEXT    NOT NULL,
    payload             TEXT    NOT NULL,
    UNIQUE (scope_type, scope_id, stream_revision)
);

CREATE INDEX IF NOT EXISTS idx_tag_scope_key
ON tag(scope_type, scope_id, key);

CREATE INDEX IF NOT EXISTS idx_tag_group_key
ON tag(tag_group_id, key);
