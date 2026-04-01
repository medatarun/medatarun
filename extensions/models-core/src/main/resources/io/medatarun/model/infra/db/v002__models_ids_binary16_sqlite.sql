PRAGMA foreign_keys = OFF;

CREATE TABLE IF NOT EXISTS model_v003
(
    id BINARY(16) PRIMARY KEY UNIQUE
);

INSERT INTO model_v003 (id)
SELECT unhex(replace(id, '-', ''))
FROM model;

CREATE TABLE IF NOT EXISTS model_event_v003
(
    id                  BINARY(16) PRIMARY KEY UNIQUE,
    model_id            BINARY(16) NOT NULL,
    stream_revision     INTEGER    NOT NULL,
    event_type          TEXT       NOT NULL,
    event_version       INTEGER    NOT NULL,
    model_version       TEXT,
    actor_id            BINARY(16) NOT NULL,
    traceability_origin TEXT       NOT NULL,
    created_at          TIMESTAMP       NOT NULL,
    payload             TEXT       NOT NULL,
    FOREIGN KEY (model_id) REFERENCES model_v003 (id) ON DELETE CASCADE,
    UNIQUE (model_id, stream_revision)
);

INSERT INTO model_event_v003 (id, model_id, stream_revision, event_type, event_version, model_version, actor_id, traceability_origin, created_at, payload)
SELECT
    unhex(replace(id, '-', '')),
    unhex(replace(model_id, '-', '')),
    stream_revision,
    event_type,
    event_version,
    model_version,
    unhex(replace(actor_id, '-', '')),
    traceability_origin,
    created_at,
    payload
FROM model_event;

CREATE TABLE IF NOT EXISTS model_snapshot_v003
(
    id                     BINARY(16) PRIMARY KEY UNIQUE,
    model_id               BINARY(16) NOT NULL,
    key                    TEXT       NOT NULL,
    name                   TEXT,
    description            TEXT,
    origin                 TEXT       NOT NULL,
    authority              TEXT       NOT NULL,
    documentation_home     TEXT,
    snapshot_kind          TEXT       NOT NULL,
    up_to_revision         INTEGER    NOT NULL,
    model_event_release_id BINARY(16),
    version                TEXT       NOT NULL,
    created_at             TIMESTAMP       NOT NULL,
    updated_at             TIMESTAMP       NOT NULL,
    FOREIGN KEY (model_id) REFERENCES model_v003 (id) ON DELETE CASCADE,
    FOREIGN KEY (model_event_release_id) REFERENCES model_event_v003 (id) ON DELETE CASCADE
);

INSERT INTO model_snapshot_v003 (id, model_id, key, name, description, origin, authority, documentation_home, snapshot_kind, up_to_revision, model_event_release_id, version, created_at, updated_at)
SELECT
    unhex(replace(id, '-', '')),
    unhex(replace(model_id, '-', '')),
    key,
    name,
    description,
    origin,
    authority,
    documentation_home,
    snapshot_kind,
    up_to_revision,
    CASE
        WHEN model_event_release_id IS NULL THEN NULL
        ELSE unhex(replace(model_event_release_id, '-', ''))
    END,
    version,
    created_at,
    updated_at
FROM model_snapshot;

CREATE TABLE IF NOT EXISTS model_tag_snapshot_v003
(
    model_snapshot_id BINARY(16) NOT NULL,
    tag_id            BINARY(16) NOT NULL,
    PRIMARY KEY (model_snapshot_id, tag_id),
    FOREIGN KEY (model_snapshot_id) REFERENCES model_snapshot_v003 (id) ON DELETE CASCADE
);

INSERT INTO model_tag_snapshot_v003 (model_snapshot_id, tag_id)
SELECT
    unhex(replace(model_snapshot_id, '-', '')),
    unhex(replace(tag_id, '-', ''))
FROM model_tag_snapshot;

CREATE TABLE IF NOT EXISTS model_type_snapshot_v003
(
    id                BINARY(16) PRIMARY KEY UNIQUE,
    lineage_id        BINARY(16) NOT NULL,
    model_snapshot_id BINARY(16) NOT NULL,
    key               TEXT       NOT NULL,
    name              TEXT,
    description       TEXT,
    FOREIGN KEY (model_snapshot_id) REFERENCES model_snapshot_v003 (id) ON DELETE CASCADE,
    UNIQUE (model_snapshot_id, lineage_id),
    UNIQUE (model_snapshot_id, key)
);

INSERT INTO model_type_snapshot_v003 (id, lineage_id, model_snapshot_id, key, name, description)
SELECT
    unhex(replace(id, '-', '')),
    unhex(replace(lineage_id, '-', '')),
    unhex(replace(model_snapshot_id, '-', '')),
    key,
    name,
    description
FROM model_type_snapshot;

CREATE TABLE IF NOT EXISTS model_entity_snapshot_v003
(
    id                               BINARY(16) PRIMARY KEY UNIQUE,
    lineage_id                       BINARY(16) NOT NULL,
    model_snapshot_id                BINARY(16) NOT NULL,
    key                              TEXT       NOT NULL,
    name                             TEXT,
    description                      TEXT,
    identifier_attribute_snapshot_id BINARY(16) NOT NULL,
    origin                           TEXT       NOT NULL,
    documentation_home               TEXT,
    FOREIGN KEY (model_snapshot_id) REFERENCES model_snapshot_v003 (id) ON DELETE CASCADE,
    UNIQUE (model_snapshot_id, lineage_id),
    UNIQUE (model_snapshot_id, key)
);

INSERT INTO model_entity_snapshot_v003 (id, lineage_id, model_snapshot_id, key, name, description, identifier_attribute_snapshot_id, origin, documentation_home)
SELECT
    unhex(replace(id, '-', '')),
    unhex(replace(lineage_id, '-', '')),
    unhex(replace(model_snapshot_id, '-', '')),
    key,
    name,
    description,
    unhex(replace(identifier_attribute_snapshot_id, '-', '')),
    origin,
    documentation_home
FROM model_entity_snapshot;

CREATE TABLE IF NOT EXISTS model_entity_tag_snapshot_v003
(
    model_entity_snapshot_id BINARY(16) NOT NULL,
    tag_id                   BINARY(16) NOT NULL,
    PRIMARY KEY (model_entity_snapshot_id, tag_id),
    FOREIGN KEY (model_entity_snapshot_id) REFERENCES model_entity_snapshot_v003 (id) ON DELETE CASCADE
);

INSERT INTO model_entity_tag_snapshot_v003 (model_entity_snapshot_id, tag_id)
SELECT
    unhex(replace(model_entity_snapshot_id, '-', '')),
    unhex(replace(tag_id, '-', ''))
FROM model_entity_tag_snapshot;

CREATE TABLE IF NOT EXISTS model_entity_attribute_snapshot_v003
(
    id                       BINARY(16) PRIMARY KEY UNIQUE,
    lineage_id               BINARY(16) NOT NULL,
    model_entity_snapshot_id BINARY(16) NOT NULL,
    key                      TEXT       NOT NULL,
    name                     TEXT,
    description              TEXT,
    model_type_snapshot_id   BINARY(16) NOT NULL,
    optional                 INTEGER    NOT NULL,
    FOREIGN KEY (model_entity_snapshot_id) REFERENCES model_entity_snapshot_v003 (id) ON DELETE CASCADE,
    FOREIGN KEY (model_type_snapshot_id) REFERENCES model_type_snapshot_v003 (id),
    UNIQUE (model_entity_snapshot_id, lineage_id),
    UNIQUE (model_entity_snapshot_id, key)
);

INSERT INTO model_entity_attribute_snapshot_v003 (id, lineage_id, model_entity_snapshot_id, key, name, description, model_type_snapshot_id, optional)
SELECT
    unhex(replace(id, '-', '')),
    unhex(replace(lineage_id, '-', '')),
    unhex(replace(model_entity_snapshot_id, '-', '')),
    key,
    name,
    description,
    unhex(replace(model_type_snapshot_id, '-', '')),
    optional
FROM model_entity_attribute_snapshot;

CREATE TABLE IF NOT EXISTS model_entity_attribute_tag_snapshot_v003
(
    model_entity_attribute_snapshot_id BINARY(16) NOT NULL,
    tag_id                             BINARY(16) NOT NULL,
    PRIMARY KEY (model_entity_attribute_snapshot_id, tag_id),
    FOREIGN KEY (model_entity_attribute_snapshot_id) REFERENCES model_entity_attribute_snapshot_v003 (id) ON DELETE CASCADE
);

INSERT INTO model_entity_attribute_tag_snapshot_v003 (model_entity_attribute_snapshot_id, tag_id)
SELECT
    unhex(replace(model_entity_attribute_snapshot_id, '-', '')),
    unhex(replace(tag_id, '-', ''))
FROM model_entity_attribute_tag_snapshot;

CREATE TABLE IF NOT EXISTS model_relationship_snapshot_v003
(
    id                BINARY(16) PRIMARY KEY UNIQUE,
    lineage_id        BINARY(16) NOT NULL,
    model_snapshot_id BINARY(16) NOT NULL,
    key               TEXT       NOT NULL,
    name              TEXT,
    description       TEXT,
    FOREIGN KEY (model_snapshot_id) REFERENCES model_snapshot_v003 (id) ON DELETE CASCADE,
    UNIQUE (model_snapshot_id, lineage_id),
    UNIQUE (model_snapshot_id, key)
);

INSERT INTO model_relationship_snapshot_v003 (id, lineage_id, model_snapshot_id, key, name, description)
SELECT
    unhex(replace(id, '-', '')),
    unhex(replace(lineage_id, '-', '')),
    unhex(replace(model_snapshot_id, '-', '')),
    key,
    name,
    description
FROM model_relationship_snapshot;

CREATE TABLE IF NOT EXISTS model_relationship_tag_snapshot_v003
(
    model_relationship_snapshot_id BINARY(16) NOT NULL,
    tag_id                         BINARY(16) NOT NULL,
    PRIMARY KEY (model_relationship_snapshot_id, tag_id),
    FOREIGN KEY (model_relationship_snapshot_id) REFERENCES model_relationship_snapshot_v003 (id) ON DELETE CASCADE
);

INSERT INTO model_relationship_tag_snapshot_v003 (model_relationship_snapshot_id, tag_id)
SELECT
    unhex(replace(model_relationship_snapshot_id, '-', '')),
    unhex(replace(tag_id, '-', ''))
FROM model_relationship_tag_snapshot;

CREATE TABLE IF NOT EXISTS model_relationship_role_snapshot_v003
(
    id                             BINARY(16) PRIMARY KEY UNIQUE,
    lineage_id                     BINARY(16) NOT NULL,
    model_relationship_snapshot_id BINARY(16) NOT NULL,
    key                            TEXT       NOT NULL,
    model_entity_snapshot_id       BINARY(16) NOT NULL,
    name                           TEXT,
    cardinality                    TEXT       NOT NULL,
    FOREIGN KEY (model_relationship_snapshot_id) REFERENCES model_relationship_snapshot_v003 (id) ON DELETE CASCADE,
    FOREIGN KEY (model_entity_snapshot_id) REFERENCES model_entity_snapshot_v003 (id),
    UNIQUE (model_relationship_snapshot_id, lineage_id),
    UNIQUE (model_relationship_snapshot_id, key)
);

INSERT INTO model_relationship_role_snapshot_v003 (id, lineage_id, model_relationship_snapshot_id, key, model_entity_snapshot_id, name, cardinality)
SELECT
    unhex(replace(id, '-', '')),
    unhex(replace(lineage_id, '-', '')),
    unhex(replace(model_relationship_snapshot_id, '-', '')),
    key,
    unhex(replace(model_entity_snapshot_id, '-', '')),
    name,
    cardinality
FROM model_relationship_role_snapshot;

CREATE TABLE IF NOT EXISTS model_relationship_attribute_snapshot_v003
(
    id                             BINARY(16) PRIMARY KEY UNIQUE,
    lineage_id                     BINARY(16) NOT NULL,
    model_relationship_snapshot_id BINARY(16) NOT NULL,
    key                            TEXT       NOT NULL,
    name                           TEXT,
    description                    TEXT,
    model_type_snapshot_id         BINARY(16) NOT NULL,
    optional                       INTEGER    NOT NULL,
    FOREIGN KEY (model_relationship_snapshot_id) REFERENCES model_relationship_snapshot_v003 (id) ON DELETE CASCADE,
    FOREIGN KEY (model_type_snapshot_id) REFERENCES model_type_snapshot_v003 (id),
    UNIQUE (model_relationship_snapshot_id, lineage_id),
    UNIQUE (model_relationship_snapshot_id, key)
);

INSERT INTO model_relationship_attribute_snapshot_v003 (id, lineage_id, model_relationship_snapshot_id, key, name, description, model_type_snapshot_id, optional)
SELECT
    unhex(replace(id, '-', '')),
    unhex(replace(lineage_id, '-', '')),
    unhex(replace(model_relationship_snapshot_id, '-', '')),
    key,
    name,
    description,
    unhex(replace(model_type_snapshot_id, '-', '')),
    optional
FROM model_relationship_attribute_snapshot;

CREATE TABLE IF NOT EXISTS model_relationship_attribute_tag_snapshot_v003
(
    model_relationship_attribute_snapshot_id BINARY(16) NOT NULL,
    tag_id                                   BINARY(16) NOT NULL,
    PRIMARY KEY (model_relationship_attribute_snapshot_id, tag_id),
    FOREIGN KEY (model_relationship_attribute_snapshot_id) REFERENCES model_relationship_attribute_snapshot_v003 (id) ON DELETE CASCADE
);

INSERT INTO model_relationship_attribute_tag_snapshot_v003 (model_relationship_attribute_snapshot_id, tag_id)
SELECT
    unhex(replace(model_relationship_attribute_snapshot_id, '-', '')),
    unhex(replace(tag_id, '-', ''))
FROM model_relationship_attribute_tag_snapshot;

CREATE TABLE IF NOT EXISTS model_search_item_snapshot_v003
(
    id                 BINARY(16) PRIMARY KEY UNIQUE,
    item_type          TEXT       NOT NULL,
    model_snapshot_id  BINARY(16) NOT NULL,
    model_key          TEXT       NOT NULL,
    model_label        TEXT       NOT NULL,
    entity_id          BINARY(16),
    entity_key         TEXT,
    entity_label       TEXT,
    relationship_id    BINARY(16),
    relationship_key   TEXT,
    relationship_label TEXT,
    attribute_id       BINARY(16),
    attribute_key      TEXT,
    attribute_label    TEXT,
    search_text        TEXT       NOT NULL,
    FOREIGN KEY (model_snapshot_id) REFERENCES model_snapshot_v003 (id) ON DELETE CASCADE
);

INSERT INTO model_search_item_snapshot_v003 (id, item_type, model_snapshot_id, model_key, model_label, entity_id, entity_key, entity_label, relationship_id, relationship_key, relationship_label, attribute_id, attribute_key, attribute_label, search_text)
SELECT
    CASE item_type
        WHEN 'model' THEN unhex(replace(substr(id, 7), '-', ''))
        WHEN 'entity' THEN unhex(replace(substr(id, 8), '-', ''))
        WHEN 'entity_attribute' THEN unhex(replace(substr(id, 18), '-', ''))
        WHEN 'relationship' THEN unhex(replace(substr(id, 14), '-', ''))
        WHEN 'relationship_attribute' THEN unhex(replace(substr(id, 24), '-', ''))
    END,
    item_type,
    unhex(replace(model_snapshot_id, '-', '')),
    model_key,
    model_label,
    CASE
        WHEN entity_id IS NULL THEN NULL
        ELSE unhex(replace(entity_id, '-', ''))
    END,
    entity_key,
    entity_label,
    CASE
        WHEN relationship_id IS NULL THEN NULL
        ELSE unhex(replace(relationship_id, '-', ''))
    END,
    relationship_key,
    relationship_label,
    CASE
        WHEN attribute_id IS NULL THEN NULL
        ELSE unhex(replace(attribute_id, '-', ''))
    END,
    attribute_key,
    attribute_label,
    search_text
FROM model_search_item_snapshot;

CREATE TABLE IF NOT EXISTS model_search_item_tag_snapshot_v003
(
    search_item_id BINARY(16) NOT NULL,
    tag_id         BINARY(16) NOT NULL,
    PRIMARY KEY (search_item_id, tag_id),
    FOREIGN KEY (search_item_id) REFERENCES model_search_item_snapshot_v003 (id) ON DELETE CASCADE
);

INSERT INTO model_search_item_tag_snapshot_v003 (search_item_id, tag_id)
SELECT
    CASE
        WHEN search_item_id LIKE 'model:%' THEN unhex(replace(substr(search_item_id, 7), '-', ''))
        WHEN search_item_id LIKE 'entity:%' THEN unhex(replace(substr(search_item_id, 8), '-', ''))
        WHEN search_item_id LIKE 'entity_attribute:%' THEN unhex(replace(substr(search_item_id, 18), '-', ''))
        WHEN search_item_id LIKE 'relationship:%' THEN unhex(replace(substr(search_item_id, 14), '-', ''))
        WHEN search_item_id LIKE 'relationship_attribute:%' THEN unhex(replace(substr(search_item_id, 24), '-', ''))
    END,
    unhex(replace(tag_id, '-', ''))
FROM model_search_item_tag_snapshot;

DROP TABLE model_search_item_tag_snapshot;
DROP TABLE model_search_item_snapshot;
DROP TABLE model_relationship_attribute_tag_snapshot;
DROP TABLE model_relationship_attribute_snapshot;
DROP TABLE model_relationship_role_snapshot;
DROP TABLE model_relationship_tag_snapshot;
DROP TABLE model_relationship_snapshot;
DROP TABLE model_entity_attribute_tag_snapshot;
DROP TABLE model_entity_attribute_snapshot;
DROP TABLE model_entity_tag_snapshot;
DROP TABLE model_entity_snapshot;
DROP TABLE model_type_snapshot;
DROP TABLE model_tag_snapshot;
DROP TABLE model_snapshot;
DROP TABLE model_event;
DROP TABLE model;

ALTER TABLE model_v003 RENAME TO model;
ALTER TABLE model_event_v003 RENAME TO model_event;
ALTER TABLE model_snapshot_v003 RENAME TO model_snapshot;
ALTER TABLE model_tag_snapshot_v003 RENAME TO model_tag_snapshot;
ALTER TABLE model_type_snapshot_v003 RENAME TO model_type_snapshot;
ALTER TABLE model_entity_snapshot_v003 RENAME TO model_entity_snapshot;
ALTER TABLE model_entity_tag_snapshot_v003 RENAME TO model_entity_tag_snapshot;
ALTER TABLE model_entity_attribute_snapshot_v003 RENAME TO model_entity_attribute_snapshot;
ALTER TABLE model_entity_attribute_tag_snapshot_v003 RENAME TO model_entity_attribute_tag_snapshot;
ALTER TABLE model_relationship_snapshot_v003 RENAME TO model_relationship_snapshot;
ALTER TABLE model_relationship_tag_snapshot_v003 RENAME TO model_relationship_tag_snapshot;
ALTER TABLE model_relationship_role_snapshot_v003 RENAME TO model_relationship_role_snapshot;
ALTER TABLE model_relationship_attribute_snapshot_v003 RENAME TO model_relationship_attribute_snapshot;
ALTER TABLE model_relationship_attribute_tag_snapshot_v003 RENAME TO model_relationship_attribute_tag_snapshot;
ALTER TABLE model_search_item_snapshot_v003 RENAME TO model_search_item_snapshot;
ALTER TABLE model_search_item_tag_snapshot_v003 RENAME TO model_search_item_tag_snapshot;

CREATE INDEX IF NOT EXISTS idx_model_type_model_id
    ON model_type_snapshot (model_snapshot_id);

CREATE INDEX IF NOT EXISTS idx_model_event_model_id
    ON model_event (model_id);

CREATE UNIQUE INDEX IF NOT EXISTS ux_model_event_release_model_version
    ON model_event (model_id, model_version)
    WHERE event_type = 'model_release' AND model_version IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_model_snapshot_current_head_model_id
    ON model_snapshot (model_id)
    WHERE snapshot_kind = 'CURRENT_HEAD';

CREATE UNIQUE INDEX IF NOT EXISTS ux_model_snapshot_current_head_key
    ON model_snapshot (key)
    WHERE snapshot_kind = 'CURRENT_HEAD';

CREATE UNIQUE INDEX IF NOT EXISTS ux_model_snapshot_version_snapshot_release_event_id
    ON model_snapshot (model_event_release_id)
    WHERE snapshot_kind = 'VERSION_SNAPSHOT' AND model_event_release_id IS NOT NULL;

PRAGMA foreign_keys = ON;
