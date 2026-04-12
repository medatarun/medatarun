CREATE TABLE model
(
    id BINARY(16) PRIMARY KEY UNIQUE
);

CREATE TABLE model_entity_attribute_snapshot
(
    id BINARY(16) PRIMARY KEY UNIQUE,
    lineage_id BINARY(16) NOT NULL,
    model_entity_snapshot_id BINARY(16) NOT NULL,
    key TEXT NOT NULL,
    name TEXT,
    description TEXT,
    model_type_snapshot_id BINARY(16) NOT NULL,
    optional INTEGER NOT NULL,
    FOREIGN KEY (model_entity_snapshot_id) REFERENCES model_entity_snapshot (id) ON DELETE CASCADE,
    FOREIGN KEY (model_type_snapshot_id) REFERENCES model_type_snapshot (id) ON DELETE CASCADE,
    UNIQUE (model_entity_snapshot_id, lineage_id),
    UNIQUE (model_entity_snapshot_id, key)
);

CREATE TABLE model_entity_attribute_tag_snapshot
(
    model_entity_attribute_snapshot_id BINARY(16) NOT NULL,
    tag_id BINARY(16) NOT NULL,
    PRIMARY KEY (model_entity_attribute_snapshot_id, tag_id),
    FOREIGN KEY (model_entity_attribute_snapshot_id) REFERENCES model_entity_attribute_snapshot (id) ON DELETE CASCADE
);

CREATE TABLE model_entity_snapshot
(
    id BINARY(16) PRIMARY KEY UNIQUE,
    lineage_id BINARY(16) NOT NULL,
    model_snapshot_id BINARY(16) NOT NULL,
    key TEXT NOT NULL,
    name TEXT,
    description TEXT,
    identifier_attribute_snapshot_id BINARY(16) NOT NULL,
    origin TEXT NOT NULL,
    documentation_home TEXT,
    FOREIGN KEY (model_snapshot_id) REFERENCES model_snapshot (id) ON DELETE CASCADE,
    UNIQUE (model_snapshot_id, lineage_id),
    UNIQUE (model_snapshot_id, key)
);

CREATE TABLE model_entity_pk_snapshot
(
    id BINARY(16) PRIMARY KEY UNIQUE,
    lineage_id BINARY(16) NOT NULL,
    model_entity_snapshot_id BINARY(16) NOT NULL,
    FOREIGN KEY (model_entity_snapshot_id) REFERENCES model_entity_snapshot (id) ON DELETE CASCADE
);

CREATE TABLE model_entity_pk_attribute_snapshot
(
    model_entity_pk_snapshot_id BINARY(16) NOT NULL,
    priority INTEGER NOT NULL,
    model_entity_attribute_snapshot_id BINARY(16) NOT NULL,
    FOREIGN KEY (model_entity_pk_snapshot_id) REFERENCES model_entity_pk_snapshot (id) ON DELETE CASCADE,
    FOREIGN KEY (model_entity_attribute_snapshot_id) REFERENCES model_entity_attribute_snapshot (id) ON DELETE CASCADE,
    UNIQUE (model_entity_pk_snapshot_id, model_entity_attribute_snapshot_id),
    UNIQUE (model_entity_pk_snapshot_id, priority)
);

CREATE TABLE model_business_key_snapshot
(
    id BINARY(16) PRIMARY KEY UNIQUE,
    lineage_id BINARY(16) NOT NULL,
    model_entity_snapshot_id BINARY(16) NOT NULL,
    key TEXT NOT NULL,
    name TEXT,
    description TEXT,
    FOREIGN KEY (model_entity_snapshot_id) REFERENCES model_entity_snapshot (id) ON DELETE CASCADE,
    UNIQUE (model_entity_snapshot_id, key)
);

CREATE TABLE model_business_key_attribute_snapshot
(
    model_business_key_snapshot_id BINARY(16) NOT NULL,
    priority INTEGER NOT NULL,
    model_entity_attribute_snapshot_id BINARY(16) NOT NULL,
    FOREIGN KEY (model_business_key_snapshot_id) REFERENCES model_business_key_snapshot (id) ON DELETE CASCADE,
    FOREIGN KEY (model_entity_attribute_snapshot_id) REFERENCES model_entity_attribute_snapshot (id) ON DELETE CASCADE,
    UNIQUE (model_business_key_snapshot_id, model_entity_attribute_snapshot_id),
    UNIQUE (model_business_key_snapshot_id, priority)
);

CREATE TABLE model_entity_tag_snapshot
(
    model_entity_snapshot_id BINARY(16) NOT NULL,
    tag_id BINARY(16) NOT NULL,
    PRIMARY KEY (model_entity_snapshot_id, tag_id),
    FOREIGN KEY (model_entity_snapshot_id) REFERENCES model_entity_snapshot (id) ON DELETE CASCADE
);

CREATE TABLE model_event
(
    id BINARY(16) PRIMARY KEY UNIQUE,
    model_id BINARY(16) NOT NULL,
    stream_revision INTEGER NOT NULL,
    event_type TEXT NOT NULL,
    event_version INTEGER NOT NULL,
    model_version TEXT,
    actor_id BINARY(16) NOT NULL,
    traceability_origin TEXT NOT NULL,
    created_at INTEGER NOT NULL,
    payload TEXT NOT NULL,
    FOREIGN KEY (model_id) REFERENCES model (id) ON DELETE CASCADE,
    UNIQUE (model_id, stream_revision)
);

CREATE TABLE model_relationship_attribute_snapshot
(
    id BINARY(16) PRIMARY KEY UNIQUE,
    lineage_id BINARY(16) NOT NULL,
    model_relationship_snapshot_id BINARY(16) NOT NULL,
    key TEXT NOT NULL,
    name TEXT,
    description TEXT,
    model_type_snapshot_id BINARY(16) NOT NULL,
    optional INTEGER NOT NULL,
    FOREIGN KEY (model_relationship_snapshot_id) REFERENCES model_relationship_snapshot (id) ON DELETE CASCADE,
    FOREIGN KEY (model_type_snapshot_id) REFERENCES model_type_snapshot (id) ON DELETE CASCADE,
    UNIQUE (model_relationship_snapshot_id, lineage_id),
    UNIQUE (model_relationship_snapshot_id, key)
);

CREATE TABLE model_relationship_attribute_tag_snapshot
(
    model_relationship_attribute_snapshot_id BINARY(16) NOT NULL,
    tag_id BINARY(16) NOT NULL,
    PRIMARY KEY (model_relationship_attribute_snapshot_id, tag_id),
    FOREIGN KEY (model_relationship_attribute_snapshot_id) REFERENCES model_relationship_attribute_snapshot (id) ON DELETE CASCADE
);

CREATE TABLE model_relationship_role_snapshot
(
    id BINARY(16) PRIMARY KEY UNIQUE,
    lineage_id BINARY(16) NOT NULL,
    model_relationship_snapshot_id BINARY(16) NOT NULL,
    key TEXT NOT NULL,
    model_entity_snapshot_id BINARY(16) NOT NULL,
    name TEXT,
    cardinality TEXT NOT NULL,
    FOREIGN KEY (model_relationship_snapshot_id) REFERENCES model_relationship_snapshot (id) ON DELETE CASCADE,
    FOREIGN KEY (model_entity_snapshot_id) REFERENCES model_entity_snapshot (id) ON DELETE CASCADE,
    UNIQUE (model_relationship_snapshot_id, lineage_id),
    UNIQUE (model_relationship_snapshot_id, key)
);

CREATE TABLE model_relationship_snapshot
(
    id BINARY(16) PRIMARY KEY UNIQUE,
    lineage_id BINARY(16) NOT NULL,
    model_snapshot_id BINARY(16) NOT NULL,
    key TEXT NOT NULL,
    name TEXT,
    description TEXT,
    FOREIGN KEY (model_snapshot_id) REFERENCES model_snapshot (id) ON DELETE CASCADE,
    UNIQUE (model_snapshot_id, lineage_id),
    UNIQUE (model_snapshot_id, key)
);

CREATE TABLE model_relationship_tag_snapshot
(
    model_relationship_snapshot_id BINARY(16) NOT NULL,
    tag_id BINARY(16) NOT NULL,
    PRIMARY KEY (model_relationship_snapshot_id, tag_id),
    FOREIGN KEY (model_relationship_snapshot_id) REFERENCES model_relationship_snapshot (id) ON DELETE CASCADE
);

CREATE TABLE model_search_item_snapshot
(
    id BINARY(16) PRIMARY KEY UNIQUE,
    item_type TEXT NOT NULL,
    model_snapshot_id BINARY(16) NOT NULL,
    model_key TEXT NOT NULL,
    model_label TEXT NOT NULL,
    entity_id BINARY(16),
    entity_key TEXT,
    entity_label TEXT,
    relationship_id BINARY(16),
    relationship_key TEXT,
    relationship_label TEXT,
    attribute_id BINARY(16),
    attribute_key TEXT,
    attribute_label TEXT,
    search_text TEXT NOT NULL,
    FOREIGN KEY (model_snapshot_id) REFERENCES model_snapshot (id) ON DELETE CASCADE
);

CREATE TABLE model_search_item_tag_snapshot
(
    search_item_id BINARY(16) NOT NULL,
    tag_id BINARY(16) NOT NULL,
    PRIMARY KEY (search_item_id, tag_id),
    FOREIGN KEY (search_item_id) REFERENCES model_search_item_snapshot (id) ON DELETE CASCADE
);

CREATE TABLE model_snapshot
(
    id BINARY(16) PRIMARY KEY UNIQUE,
    model_id BINARY(16) NOT NULL,
    key TEXT NOT NULL,
    name TEXT,
    description TEXT,
    origin TEXT NOT NULL,
    authority TEXT NOT NULL,
    documentation_home TEXT,
    snapshot_kind TEXT NOT NULL,
    up_to_revision INTEGER NOT NULL,
    model_event_release_id BINARY(16),
    version TEXT NOT NULL,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    FOREIGN KEY (model_id) REFERENCES model (id) ON DELETE CASCADE,
    FOREIGN KEY (model_event_release_id) REFERENCES model_event (id) ON DELETE CASCADE
);

CREATE TABLE model_tag_snapshot
(
    model_snapshot_id BINARY(16) NOT NULL,
    tag_id BINARY(16) NOT NULL,
    PRIMARY KEY (model_snapshot_id, tag_id),
    FOREIGN KEY (model_snapshot_id) REFERENCES model_snapshot (id) ON DELETE CASCADE
);

CREATE TABLE model_type_snapshot
(
    id BINARY(16) PRIMARY KEY UNIQUE,
    lineage_id BINARY(16) NOT NULL,
    model_snapshot_id BINARY(16) NOT NULL,
    key TEXT NOT NULL,
    name TEXT,
    description TEXT,
    FOREIGN KEY (model_snapshot_id) REFERENCES model_snapshot (id) ON DELETE CASCADE,
    UNIQUE (model_snapshot_id, lineage_id),
    UNIQUE (model_snapshot_id, key)
);

CREATE INDEX idx_model_event_model_id
ON model_event (model_id);
CREATE INDEX idx_model_type_model_id
ON model_type_snapshot (model_snapshot_id);
CREATE UNIQUE INDEX ux_model_event_release_model_version
ON model_event (model_id, model_version)
WHERE event_type = 'model_release' AND model_version IS NOT NULL;
CREATE UNIQUE INDEX ux_model_snapshot_current_head_key
ON model_snapshot (key)
WHERE snapshot_kind = 'CURRENT_HEAD';
CREATE UNIQUE INDEX ux_model_snapshot_current_head_model_id
ON model_snapshot (model_id)
WHERE snapshot_kind = 'CURRENT_HEAD';
CREATE UNIQUE INDEX ux_model_snapshot_version_snapshot_release_event_id
ON model_snapshot (model_event_release_id)
WHERE snapshot_kind = 'VERSION_SNAPSHOT' AND model_event_release_id IS NOT NULL;
