CREATE TABLE model_entity_pk_snapshot (
    id UUID PRIMARY KEY,
    lineage_id UUID NOT NULL,
    model_entity_snapshot_id UUID NOT NULL,
    FOREIGN KEY (model_entity_snapshot_id) REFERENCES model_entity_snapshot (id) ON DELETE CASCADE
);

CREATE TABLE model_entity_pk_attribute_snapshot (
    model_entity_pk_snapshot_id UUID NOT NULL,
    priority INTEGER NOT NULL,
    model_entity_attribute_snapshot_id UUID NOT NULL,
    FOREIGN KEY (model_entity_pk_snapshot_id) REFERENCES model_entity_pk_snapshot (id) ON DELETE CASCADE,
    FOREIGN KEY (model_entity_attribute_snapshot_id) REFERENCES model_entity_attribute_snapshot (id) ON DELETE CASCADE,
    UNIQUE (model_entity_pk_snapshot_id, model_entity_attribute_snapshot_id),
    UNIQUE (model_entity_pk_snapshot_id, priority)
);

CREATE TABLE model_business_key_snapshot (
    id UUID PRIMARY KEY,
    lineage_id UUID NOT NULL,
    model_entity_snapshot_id UUID NOT NULL,
    key TEXT NOT NULL,
    name TEXT,
    description TEXT,
    FOREIGN KEY (model_entity_snapshot_id) REFERENCES model_entity_snapshot (id) ON DELETE CASCADE,
    UNIQUE (model_entity_snapshot_id, key)
);

CREATE TABLE model_business_key_attribute_snapshot (
    model_business_key_snapshot_id UUID NOT NULL,
    priority INTEGER NOT NULL,
    model_entity_attribute_snapshot_id UUID NOT NULL,
    FOREIGN KEY (model_business_key_snapshot_id) REFERENCES model_business_key_snapshot (id) ON DELETE CASCADE,
    FOREIGN KEY (model_entity_attribute_snapshot_id) REFERENCES model_entity_attribute_snapshot (id) ON DELETE CASCADE,
    UNIQUE (model_business_key_snapshot_id, model_entity_attribute_snapshot_id),
    UNIQUE (model_business_key_snapshot_id, priority)
);
