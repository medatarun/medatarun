CREATE TABLE IF NOT EXISTS model
(
    id TEXT PRIMARY KEY UNIQUE
);

CREATE TABLE IF NOT EXISTS model_event
(
    id              TEXT PRIMARY KEY UNIQUE,
    model_id        TEXT    NOT NULL,
    stream_revision INTEGER NOT NULL,
    event_type      TEXT    NOT NULL,
    event_version   INTEGER NOT NULL,
    model_version   TEXT,
    actor_id        TEXT    NOT NULL,
    action_id       TEXT    NOT NULL,
    created_at      TEXT    NOT NULL,
    payload         TEXT    NOT NULL,
    FOREIGN KEY (model_id) REFERENCES model (id) ON DELETE CASCADE,
    UNIQUE (model_id, stream_revision)
);

CREATE TABLE IF NOT EXISTS model_snapshot
(
    id                     TEXT PRIMARY KEY UNIQUE,
    model_id               TEXT    NOT NULL,
    key                    TEXT    NOT NULL,
    name                   TEXT,
    description            TEXT,
    origin                 TEXT    NOT NULL,
    authority              TEXT    NOT NULL,
    documentation_home     TEXT,
    snapshot_kind          TEXT    NOT NULL,
    up_to_revision         INTEGER NOT NULL,
    model_event_release_id TEXT,
    version                TEXT,
    created_at             TEXT    NOT NULL,
    updated_at             TEXT    NOT NULL,
    FOREIGN KEY (model_id) REFERENCES model (id) ON DELETE CASCADE,
    FOREIGN KEY (model_event_release_id) REFERENCES model_event (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS model_tag_snapshot
(
    model_snapshot_id TEXT NOT NULL,
    tag_id            TEXT NOT NULL,
    PRIMARY KEY (model_snapshot_id, tag_id),
    FOREIGN KEY (model_snapshot_id) REFERENCES model_snapshot (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS model_type_snapshot
(
    id                TEXT PRIMARY KEY UNIQUE,
    lineage_id        TEXT NOT NULL,
    model_snapshot_id TEXT NOT NULL,
    key               TEXT NOT NULL,
    name              TEXT,
    description       TEXT,
    FOREIGN KEY (model_snapshot_id) REFERENCES model_snapshot (id) ON DELETE CASCADE,
    UNIQUE (model_snapshot_id, lineage_id),
    UNIQUE (model_snapshot_id, key)
);

CREATE TABLE IF NOT EXISTS model_entity_snapshot
(
    id                             TEXT PRIMARY KEY UNIQUE,
    lineage_id                     TEXT NOT NULL,
    model_snapshot_id              TEXT NOT NULL,
    key                            TEXT NOT NULL,
    name                           TEXT,
    description                    TEXT,
    identifier_attribute_snapshot_id TEXT NOT NULL,
    origin                         TEXT NOT NULL,
    documentation_home             TEXT,
    FOREIGN KEY (model_snapshot_id) REFERENCES model_snapshot (id) ON DELETE CASCADE,
    UNIQUE (model_snapshot_id, lineage_id),
    UNIQUE (model_snapshot_id, key)
);

CREATE TABLE IF NOT EXISTS model_entity_tag_snapshot
(
    model_entity_snapshot_id TEXT NOT NULL,
    tag_id                   TEXT NOT NULL,
    PRIMARY KEY (model_entity_snapshot_id, tag_id),
    FOREIGN KEY (model_entity_snapshot_id) REFERENCES model_entity_snapshot (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS model_entity_attribute_snapshot
(
    id                       TEXT PRIMARY KEY UNIQUE,
    lineage_id               TEXT    NOT NULL,
    model_entity_snapshot_id TEXT    NOT NULL,
    key                      TEXT    NOT NULL,
    name                     TEXT,
    description              TEXT,
    model_type_snapshot_id   TEXT    NOT NULL,
    optional                 INTEGER NOT NULL,
    FOREIGN KEY (model_entity_snapshot_id) REFERENCES model_entity_snapshot (id) ON DELETE CASCADE,
    FOREIGN KEY (model_type_snapshot_id) REFERENCES model_type_snapshot (id),
    UNIQUE (model_entity_snapshot_id, lineage_id),
    UNIQUE (model_entity_snapshot_id, key)
);

CREATE TABLE IF NOT EXISTS model_entity_attribute_tag_snapshot
(
    model_entity_attribute_snapshot_id TEXT NOT NULL,
    tag_id                             TEXT NOT NULL,
    PRIMARY KEY (model_entity_attribute_snapshot_id, tag_id),
    FOREIGN KEY (model_entity_attribute_snapshot_id) REFERENCES model_entity_attribute_snapshot (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS model_relationship_snapshot
(
    id                TEXT PRIMARY KEY UNIQUE,
    lineage_id        TEXT NOT NULL,
    model_snapshot_id TEXT NOT NULL,
    key               TEXT NOT NULL,
    name              TEXT,
    description       TEXT,
    FOREIGN KEY (model_snapshot_id) REFERENCES model_snapshot (id) ON DELETE CASCADE,
    UNIQUE (model_snapshot_id, lineage_id),
    UNIQUE (model_snapshot_id, key)
);

CREATE TABLE IF NOT EXISTS model_relationship_tag_snapshot
(
    model_relationship_snapshot_id TEXT NOT NULL,
    tag_id                         TEXT NOT NULL,
    PRIMARY KEY (model_relationship_snapshot_id, tag_id),
    FOREIGN KEY (model_relationship_snapshot_id) REFERENCES model_relationship_snapshot (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS model_relationship_role_snapshot
(
    id                             TEXT PRIMARY KEY UNIQUE,
    lineage_id                     TEXT NOT NULL,
    model_relationship_snapshot_id TEXT NOT NULL,
    key                            TEXT NOT NULL,
    model_entity_snapshot_id       TEXT NOT NULL,
    name                           TEXT,
    cardinality                    TEXT NOT NULL,
    FOREIGN KEY (model_relationship_snapshot_id) REFERENCES model_relationship_snapshot (id) ON DELETE CASCADE,
    FOREIGN KEY (model_entity_snapshot_id) REFERENCES model_entity_snapshot (id),
    UNIQUE (model_relationship_snapshot_id, lineage_id),
    UNIQUE (model_relationship_snapshot_id, key)
);

CREATE TABLE IF NOT EXISTS model_relationship_attribute_snapshot
(
    id                              TEXT PRIMARY KEY UNIQUE,
    lineage_id                      TEXT    NOT NULL,
    model_relationship_snapshot_id  TEXT    NOT NULL,
    key                             TEXT    NOT NULL,
    name                            TEXT,
    description                     TEXT,
    model_type_snapshot_id          TEXT    NOT NULL,
    optional                        INTEGER NOT NULL,
    FOREIGN KEY (model_relationship_snapshot_id) REFERENCES model_relationship_snapshot (id) ON DELETE CASCADE,
    FOREIGN KEY (model_type_snapshot_id) REFERENCES model_type_snapshot (id),
    UNIQUE (model_relationship_snapshot_id, lineage_id),
    UNIQUE (model_relationship_snapshot_id, key)
);

CREATE TABLE IF NOT EXISTS model_relationship_attribute_tag_snapshot
(
    model_relationship_attribute_snapshot_id TEXT NOT NULL,
    tag_id                                   TEXT NOT NULL,
    PRIMARY KEY (model_relationship_attribute_snapshot_id, tag_id),
    FOREIGN KEY (model_relationship_attribute_snapshot_id) REFERENCES model_relationship_attribute_snapshot (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS model_search_item_snapshot
(
    id                 TEXT PRIMARY KEY UNIQUE,
    item_type          TEXT NOT NULL,
    model_snapshot_id  TEXT NOT NULL,
    model_key          TEXT NOT NULL,
    model_label        TEXT NOT NULL,
    entity_id          TEXT,
    entity_key         TEXT,
    entity_label       TEXT,
    relationship_id    TEXT,
    relationship_key   TEXT,
    relationship_label TEXT,
    attribute_id       TEXT,
    attribute_key      TEXT,
    attribute_label    TEXT,
    search_text        TEXT NOT NULL,
    FOREIGN KEY (model_snapshot_id) REFERENCES model_snapshot (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS model_search_item_tag_snapshot
(
    search_item_id TEXT NOT NULL,
    tag_id         TEXT NOT NULL,
    PRIMARY KEY (search_item_id, tag_id),
    FOREIGN KEY (search_item_id) REFERENCES model_search_item_snapshot (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_model_type_model_id
    ON model_type_snapshot (model_snapshot_id);

CREATE INDEX IF NOT EXISTS idx_model_event_model_id
    ON model_event (model_id);

CREATE UNIQUE INDEX IF NOT EXISTS ux_model_snapshot_current_head_model_id
    ON model_snapshot (model_id)
    WHERE snapshot_kind = 'CURRENT_HEAD';

CREATE UNIQUE INDEX IF NOT EXISTS ux_model_snapshot_current_head_key
    ON model_snapshot (key)
    WHERE snapshot_kind = 'CURRENT_HEAD';

CREATE UNIQUE INDEX IF NOT EXISTS ux_model_snapshot_version_snapshot_release_event_id
    ON model_snapshot (model_event_release_id)
    WHERE snapshot_kind = 'VERSION_SNAPSHOT' AND model_event_release_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_entity_model_id
    ON model_entity_snapshot (model_snapshot_id);

CREATE INDEX IF NOT EXISTS idx_entity_attribute_entity_id
    ON model_entity_attribute_snapshot (model_entity_snapshot_id);

CREATE INDEX IF NOT EXISTS idx_relationship_model_id
    ON model_relationship_snapshot (model_snapshot_id);

CREATE INDEX IF NOT EXISTS idx_relationship_role_relationship_id
    ON model_relationship_role_snapshot (model_relationship_snapshot_id);

CREATE INDEX IF NOT EXISTS idx_relationship_attribute_relationship_id
    ON model_relationship_attribute_snapshot (model_relationship_snapshot_id);

CREATE INDEX IF NOT EXISTS denorm_model_search_item_model_id_idx
    ON model_search_item_snapshot (model_snapshot_id);

CREATE INDEX IF NOT EXISTS denorm_model_search_item_entity_id_idx
    ON model_search_item_snapshot (entity_id);

CREATE INDEX IF NOT EXISTS denorm_model_search_item_relationship_id_idx
    ON model_search_item_snapshot (relationship_id);

CREATE INDEX IF NOT EXISTS denorm_model_search_item_attribute_id_idx
    ON model_search_item_snapshot (attribute_id);

CREATE INDEX IF NOT EXISTS denorm_model_search_item_tag_tag_id_idx
    ON model_search_item_tag_snapshot (tag_id);
