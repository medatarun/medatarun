CREATE TABLE IF NOT EXISTS tag_history_projection
(
    id           TEXT PRIMARY KEY UNIQUE,
    tag_event_id TEXT NOT NULL UNIQUE,
    tag_id       TEXT NOT NULL,
    scope_type   TEXT NOT NULL,
    scope_id     TEXT,
    tag_group_id TEXT,
    key          TEXT NOT NULL,
    name         TEXT,
    description  TEXT,
    valid_from   TEXT NOT NULL,
    valid_to     TEXT,
    FOREIGN KEY (tag_event_id) REFERENCES tag_event (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_tag_history_projection_lookup
    ON tag_history_projection(tag_id, valid_from, valid_to);

CREATE INDEX IF NOT EXISTS idx_tag_history_projection_scope
    ON tag_history_projection(scope_type, scope_id);

CREATE TABLE IF NOT EXISTS tag_group_history_projection
(
    id           TEXT PRIMARY KEY UNIQUE,
    tag_event_id TEXT NOT NULL UNIQUE,
    tag_group_id TEXT NOT NULL,
    key          TEXT NOT NULL,
    name         TEXT,
    description  TEXT,
    valid_from   TEXT NOT NULL,
    valid_to     TEXT,
    FOREIGN KEY (tag_event_id) REFERENCES tag_event (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_tag_group_history_projection_lookup
    ON tag_group_history_projection(tag_group_id, valid_from, valid_to);
