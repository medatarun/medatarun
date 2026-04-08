CREATE TABLE tag_event
(
    id BINARY(16) PRIMARY KEY UNIQUE,
    scope_type TEXT NOT NULL,
    scope_id BINARY(16),
    stream_revision INTEGER NOT NULL,
    event_type TEXT NOT NULL,
    event_version INTEGER NOT NULL,
    actor_id BINARY(16) NOT NULL,
    traceability_origin TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    payload TEXT NOT NULL,
    UNIQUE (scope_type, scope_id, stream_revision)
);

CREATE TABLE tag_view_current_tag
(
    id BINARY(16) PRIMARY KEY UNIQUE,
    scope_type TEXT NOT NULL,
    scope_id BINARY(16),
    tag_group_id BINARY(16),
    key TEXT NOT NULL,
    name TEXT,
    description TEXT,
    FOREIGN KEY (tag_group_id) REFERENCES tag_view_current_tag_group (id) ON DELETE CASCADE
);

CREATE TABLE tag_view_current_tag_group
(
    id BINARY(16) PRIMARY KEY UNIQUE,
    key TEXT NOT NULL UNIQUE,
    name TEXT,
    description TEXT
);

CREATE TABLE tag_view_history_tag
(
    id BINARY(16) PRIMARY KEY UNIQUE,
    tag_event_id BINARY(16) NOT NULL UNIQUE,
    tag_id BINARY(16) NOT NULL,
    scope_type TEXT NOT NULL,
    scope_id BINARY(16),
    tag_group_id BINARY(16),
    key TEXT NOT NULL,
    name TEXT,
    description TEXT,
    valid_from TIMESTAMP NOT NULL,
    valid_to TIMESTAMP,
    FOREIGN KEY (tag_event_id) REFERENCES tag_event (id) ON DELETE CASCADE
);

CREATE TABLE tag_view_history_tag_group
(
    id BINARY(16) PRIMARY KEY UNIQUE,
    tag_event_id BINARY(16) NOT NULL UNIQUE,
    tag_group_id BINARY(16) NOT NULL,
    key TEXT NOT NULL,
    name TEXT,
    description TEXT,
    valid_from TIMESTAMP NOT NULL,
    valid_to TIMESTAMP,
    FOREIGN KEY (tag_event_id) REFERENCES tag_event (id) ON DELETE CASCADE
);

CREATE INDEX idx_tag_view_current_tag__group_key ON tag_view_current_tag (tag_group_id, key);
CREATE INDEX idx_tag_view_current_tag__scope_key ON tag_view_current_tag (scope_type, scope_id, key);
CREATE INDEX idx_tag_view_history_tag__lookup
ON tag_view_history_tag (tag_id, valid_from, valid_to);
CREATE INDEX idx_tag_view_history_tag__scope
ON tag_view_history_tag (scope_type, scope_id);
CREATE INDEX idx_tag_view_history_tag_group__lookup
ON tag_view_history_tag_group (tag_group_id, valid_from, valid_to);
