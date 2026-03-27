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

ALTER TABLE tag_group RENAME TO tag_group_projection;
ALTER TABLE tag RENAME TO tag_projection;

DROP INDEX IF EXISTS idx_tag_scope_key;
DROP INDEX IF EXISTS idx_tag_group_key;
CREATE INDEX IF NOT EXISTS idx_tag_projection_scope_key ON tag_projection(scope_type, scope_id, key);
CREATE INDEX IF NOT EXISTS idx_tag_projection_group_key ON tag_projection(tag_group_id, key);
