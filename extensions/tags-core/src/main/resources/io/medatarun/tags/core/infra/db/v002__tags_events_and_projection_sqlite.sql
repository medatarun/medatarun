PRAGMA foreign_keys = OFF;

CREATE TABLE IF NOT EXISTS tag_event
(
    id                  BINARY(16) PRIMARY KEY UNIQUE,
    scope_type          TEXT    NOT NULL,
    scope_id            BINARY(16),
    stream_revision     INTEGER NOT NULL,
    event_type          TEXT    NOT NULL,
    event_version       INTEGER NOT NULL,
    actor_id            BINARY(16) NOT NULL,
    traceability_origin TEXT    NOT NULL,
    created_at          TEXT NOT NULL,
    payload             TEXT    NOT NULL,
    UNIQUE (scope_type, scope_id, stream_revision)
);

CREATE TABLE tag_view_current_tag_group
(
    id          BINARY(16) PRIMARY KEY UNIQUE,
    key         TEXT NOT NULL UNIQUE,
    name        TEXT,
    description TEXT
);

INSERT INTO tag_view_current_tag_group (id, key, name, description)
SELECT
    unhex(replace(id, '-', '')),
    key,
    name,
    description
FROM tag_group;

CREATE TABLE tag_view_current_tag
(
    id           BINARY(16) PRIMARY KEY UNIQUE,
    scope_type   TEXT NOT NULL,
    scope_id     BINARY(16),
    tag_group_id BINARY(16),
    key          TEXT NOT NULL,
    name         TEXT,
    description  TEXT,
    FOREIGN KEY (tag_group_id) REFERENCES tag_view_current_tag_group(id) ON DELETE CASCADE
);

INSERT INTO tag_view_current_tag (id, scope_type, scope_id, tag_group_id, key, name, description)
SELECT
    unhex(replace(id, '-', '')),
    scope_type,
    CASE
        WHEN scope_id IS NULL THEN NULL
        ELSE unhex(replace(scope_id, '-', ''))
    END,
    CASE
        WHEN tag_group_id IS NULL THEN NULL
        ELSE unhex(replace(tag_group_id, '-', ''))
    END,
    key,
    name,
    description
FROM tag;

CREATE INDEX IF NOT EXISTS idx_tag_view_current_tag__scope_key ON tag_view_current_tag(scope_type, scope_id, key);
CREATE INDEX IF NOT EXISTS idx_tag_view_current_tag__group_key ON tag_view_current_tag(tag_group_id, key);

PRAGMA foreign_keys = ON;
