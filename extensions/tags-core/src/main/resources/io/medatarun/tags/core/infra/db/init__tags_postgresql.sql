CREATE TABLE tag_event (
    id UUID PRIMARY KEY,
    scope_type TEXT NOT NULL,
    scope_id UUID,
    stream_revision INTEGER NOT NULL,
    event_type TEXT NOT NULL,
    event_version INTEGER NOT NULL,
    actor_id UUID NOT NULL,
    traceability_origin TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    payload JSONB NOT NULL,
    UNIQUE (scope_type, scope_id, stream_revision)
);

CREATE TABLE tag_view_current_tag_group (
    id UUID PRIMARY KEY,
    key TEXT NOT NULL UNIQUE,
    name TEXT,
    description TEXT
);

CREATE TABLE tag_view_current_tag (
    id UUID PRIMARY KEY,
    scope_type TEXT NOT NULL,
    scope_id UUID,
    tag_group_id UUID,
    key TEXT NOT NULL,
    name TEXT,
    description TEXT,
    FOREIGN KEY (tag_group_id) REFERENCES tag_view_current_tag_group(id) ON DELETE CASCADE
);

CREATE TABLE tag_view_history_tag (
    id UUID PRIMARY KEY,
    tag_event_id UUID NOT NULL UNIQUE,
    tag_id UUID NOT NULL,
    scope_type TEXT NOT NULL,
    scope_id UUID,
    tag_group_id UUID,
    key TEXT NOT NULL,
    name TEXT,
    description TEXT,
    valid_from TIMESTAMPTZ NOT NULL,
    valid_to TIMESTAMPTZ,
    FOREIGN KEY (tag_event_id) REFERENCES tag_event (id) ON DELETE CASCADE
);

CREATE TABLE tag_view_history_tag_group (
    id UUID PRIMARY KEY,
    tag_event_id UUID NOT NULL UNIQUE,
    tag_group_id UUID NOT NULL,
    key TEXT NOT NULL,
    name TEXT,
    description TEXT,
    valid_from TIMESTAMPTZ NOT NULL,
    valid_to TIMESTAMPTZ,
    FOREIGN KEY (tag_event_id) REFERENCES tag_event (id) ON DELETE CASCADE
);

CREATE INDEX idx_tag_view_current_tag__group_key ON tag_view_current_tag(tag_group_id, key);
CREATE INDEX idx_tag_view_current_tag__scope_key ON tag_view_current_tag(scope_type, scope_id, key);
CREATE INDEX idx_tag_view_history_tag__lookup ON tag_view_history_tag(tag_id, valid_from, valid_to);
CREATE INDEX idx_tag_view_history_tag__scope ON tag_view_history_tag(scope_type, scope_id);
CREATE INDEX idx_tag_view_history_tag_group__lookup ON tag_view_history_tag_group(tag_group_id, valid_from, valid_to);
