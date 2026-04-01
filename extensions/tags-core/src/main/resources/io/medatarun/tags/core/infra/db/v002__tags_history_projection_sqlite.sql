CREATE TABLE IF NOT EXISTS tag_view_history_tag
(
    id           BINARY(16) PRIMARY KEY UNIQUE,
    tag_event_id BINARY(16) NOT NULL UNIQUE,
    tag_id       BINARY(16) NOT NULL,
    scope_type   TEXT NOT NULL,
    scope_id     BINARY(16),
    tag_group_id BINARY(16),
    key          TEXT NOT NULL,
    name         TEXT,
    description  TEXT,
    valid_from   TIMESTAMP NOT NULL,
    valid_to     TIMESTAMP,
    FOREIGN KEY (tag_event_id) REFERENCES tag_event (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_tag_view_history_tag__lookup
    ON tag_view_history_tag(tag_id, valid_from, valid_to);

CREATE INDEX IF NOT EXISTS idx_tag_view_history_tag__scope
    ON tag_view_history_tag(scope_type, scope_id);

CREATE TABLE IF NOT EXISTS tag_view_history_tag_group
(
    id           BINARY(16) PRIMARY KEY UNIQUE,
    tag_event_id BINARY(16) NOT NULL UNIQUE,
    tag_group_id BINARY(16) NOT NULL,
    key          TEXT NOT NULL,
    name         TEXT,
    description  TEXT,
    valid_from   TIMESTAMP NOT NULL,
    valid_to     TIMESTAMP,
    FOREIGN KEY (tag_event_id) REFERENCES tag_event (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_tag_view_history_tag_group__lookup
    ON tag_view_history_tag_group(tag_group_id, valid_from, valid_to);
