CREATE TABLE IF NOT EXISTS model
(
    id                 TEXT PRIMARY KEY UNIQUE,
    key                TEXT NOT NULL UNIQUE,
    name               TEXT,
    description        TEXT,
    version            TEXT NOT NULL,
    origin             TEXT,
    documentation_home TEXT
);

CREATE TABLE IF NOT EXISTS model_tag
(
    model_id TEXT NOT NULL,
    tag_id   TEXT NOT NULL,
    PRIMARY KEY (model_id, tag_id),
    FOREIGN KEY (model_id) REFERENCES model (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS model_type
(
    id          TEXT PRIMARY KEY UNIQUE,
    model_id    TEXT NOT NULL,
    key         TEXT NOT NULL,
    name        TEXT,
    description TEXT,
    FOREIGN KEY (model_id) REFERENCES model (id) ON DELETE CASCADE,
    UNIQUE (model_id, key)
);

CREATE TABLE IF NOT EXISTS entity
(
    id                      TEXT PRIMARY KEY UNIQUE,
    model_id                TEXT NOT NULL,
    key                     TEXT NOT NULL,
    name                    TEXT,
    description             TEXT,
    identifier_attribute_id TEXT,
    origin                  TEXT,
    documentation_home      TEXT,
    FOREIGN KEY (model_id) REFERENCES model (id) ON DELETE CASCADE,
    UNIQUE (model_id, key)
);

CREATE TABLE IF NOT EXISTS entity_tag
(
    entity_id TEXT NOT NULL,
    tag_id    TEXT NOT NULL,
    PRIMARY KEY (entity_id, tag_id),
    FOREIGN KEY (entity_id) REFERENCES entity (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS entity_attribute
(
    id          TEXT PRIMARY KEY UNIQUE,
    entity_id   TEXT    NOT NULL,
    key         TEXT    NOT NULL,
    name        TEXT,
    description TEXT,
    type_id     TEXT    NOT NULL,
    optional    INTEGER NOT NULL,
    FOREIGN KEY (entity_id) REFERENCES entity (id) ON DELETE CASCADE,
    FOREIGN KEY (type_id) REFERENCES model_type (id),
    UNIQUE (entity_id, key)
);

CREATE TABLE IF NOT EXISTS entity_attribute_tag
(
    attribute_id TEXT NOT NULL,
    tag_id       TEXT NOT NULL,
    PRIMARY KEY (attribute_id, tag_id),
    FOREIGN KEY (attribute_id) REFERENCES entity_attribute (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS relationship
(
    id          TEXT PRIMARY KEY UNIQUE,
    model_id    TEXT NOT NULL,
    key         TEXT NOT NULL,
    name        TEXT,
    description TEXT,
    FOREIGN KEY (model_id) REFERENCES model (id) ON DELETE CASCADE,
    UNIQUE (model_id, key)
);

CREATE TABLE IF NOT EXISTS relationship_tag
(
    relationship_id TEXT NOT NULL,
    tag_id          TEXT NOT NULL,
    PRIMARY KEY (relationship_id, tag_id),
    FOREIGN KEY (relationship_id) REFERENCES relationship (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS relationship_role
(
    id              TEXT PRIMARY KEY UNIQUE,
    relationship_id TEXT NOT NULL,
    key             TEXT NOT NULL,
    entity_id       TEXT NOT NULL,
    name            TEXT,
    cardinality     TEXT NOT NULL,
    FOREIGN KEY (relationship_id) REFERENCES relationship (id) ON DELETE CASCADE,
    FOREIGN KEY (entity_id) REFERENCES entity (id),
    UNIQUE (relationship_id, key)
);

CREATE TABLE IF NOT EXISTS relationship_attribute
(
    id              TEXT PRIMARY KEY UNIQUE,
    relationship_id TEXT    NOT NULL,
    key             TEXT    NOT NULL,
    name            TEXT,
    description     TEXT,
    type_id         TEXT    NOT NULL,
    optional        INTEGER NOT NULL,
    FOREIGN KEY (relationship_id) REFERENCES relationship (id) ON DELETE CASCADE,
    FOREIGN KEY (type_id) REFERENCES model_type (id),
    UNIQUE (relationship_id, key)
);

CREATE TABLE IF NOT EXISTS relationship_attribute_tag
(
    attribute_id TEXT NOT NULL,
    tag_id       TEXT NOT NULL,
    PRIMARY KEY (attribute_id, tag_id),
    FOREIGN KEY (attribute_id) REFERENCES relationship_attribute (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS denorm_model_search_item
(
    id                 TEXT PRIMARY KEY UNIQUE,
    item_type          TEXT NOT NULL,
    model_id           TEXT NOT NULL,
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
    search_text        TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS denorm_model_search_item_tag
(
    search_item_id TEXT NOT NULL,
    tag_id         TEXT NOT NULL,
    PRIMARY KEY (search_item_id, tag_id),
    FOREIGN KEY (search_item_id) REFERENCES denorm_model_search_item (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_model_type_model_id
    ON model_type (model_id);

CREATE INDEX IF NOT EXISTS idx_entity_model_id
    ON entity (model_id);

CREATE INDEX IF NOT EXISTS idx_entity_attribute_entity_id
    ON entity_attribute (entity_id);

CREATE INDEX IF NOT EXISTS idx_relationship_model_id
    ON relationship (model_id);

CREATE INDEX IF NOT EXISTS idx_relationship_role_relationship_id
    ON relationship_role (relationship_id);

CREATE INDEX IF NOT EXISTS idx_relationship_attribute_relationship_id
    ON relationship_attribute (relationship_id);

CREATE INDEX IF NOT EXISTS denorm_model_search_item_model_id_idx
    ON denorm_model_search_item (model_id);

CREATE INDEX IF NOT EXISTS denorm_model_search_item_entity_id_idx
    ON denorm_model_search_item (entity_id);

CREATE INDEX IF NOT EXISTS denorm_model_search_item_relationship_id_idx
    ON denorm_model_search_item (relationship_id);

CREATE INDEX IF NOT EXISTS denorm_model_search_item_attribute_id_idx
    ON denorm_model_search_item (attribute_id);

CREATE INDEX IF NOT EXISTS denorm_model_search_item_tag_tag_id_idx
    ON denorm_model_search_item_tag (tag_id);
