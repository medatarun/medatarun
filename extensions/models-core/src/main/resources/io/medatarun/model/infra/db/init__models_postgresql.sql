CREATE TABLE model (
    id uuid NOT NULL
);

CREATE TABLE model_business_key_attribute_snapshot (
    model_business_key_snapshot_id uuid NOT NULL,
    priority integer NOT NULL,
    model_entity_attribute_snapshot_id uuid NOT NULL
);

CREATE TABLE model_business_key_snapshot (
    id uuid NOT NULL,
    lineage_id uuid NOT NULL,
    model_entity_snapshot_id uuid NOT NULL,
    key text NOT NULL,
    name text,
    description text
);

CREATE TABLE model_entity_attribute_snapshot (
    id uuid NOT NULL,
    lineage_id uuid NOT NULL,
    model_entity_snapshot_id uuid NOT NULL,
    key text NOT NULL,
    name text,
    description text,
    model_type_snapshot_id uuid NOT NULL,
    optional boolean NOT NULL
);

CREATE TABLE model_entity_attribute_tag_snapshot (
    model_entity_attribute_snapshot_id uuid NOT NULL,
    tag_id uuid NOT NULL
);

CREATE TABLE model_entity_pk_attribute_snapshot (
    model_entity_pk_snapshot_id uuid NOT NULL,
    priority integer NOT NULL,
    model_entity_attribute_snapshot_id uuid NOT NULL
);

CREATE TABLE model_entity_pk_snapshot (
    id uuid NOT NULL,
    lineage_id uuid NOT NULL,
    model_entity_snapshot_id uuid NOT NULL
);

CREATE TABLE model_entity_snapshot (
    id uuid NOT NULL,
    lineage_id uuid NOT NULL,
    model_snapshot_id uuid NOT NULL,
    key text NOT NULL,
    name text,
    description text,
    origin text NOT NULL,
    documentation_home text
);

CREATE TABLE model_entity_tag_snapshot (
    model_entity_snapshot_id uuid NOT NULL,
    tag_id uuid NOT NULL
);

CREATE TABLE model_event (
    id uuid NOT NULL,
    model_id uuid NOT NULL,
    stream_revision integer NOT NULL,
    event_type text NOT NULL,
    event_version integer NOT NULL,
    model_version text,
    actor_id uuid NOT NULL,
    traceability_origin text NOT NULL,
    created_at timestamp with time zone NOT NULL,
    payload jsonb NOT NULL
);

CREATE TABLE model_relationship_attribute_snapshot (
    id uuid NOT NULL,
    lineage_id uuid NOT NULL,
    model_relationship_snapshot_id uuid NOT NULL,
    key text NOT NULL,
    name text,
    description text,
    model_type_snapshot_id uuid NOT NULL,
    optional boolean NOT NULL
);

CREATE TABLE model_relationship_attribute_tag_snapshot (
    model_relationship_attribute_snapshot_id uuid NOT NULL,
    tag_id uuid NOT NULL
);

CREATE TABLE model_relationship_role_snapshot (
    id uuid NOT NULL,
    lineage_id uuid NOT NULL,
    model_relationship_snapshot_id uuid NOT NULL,
    key text NOT NULL,
    model_entity_snapshot_id uuid NOT NULL,
    name text,
    cardinality text NOT NULL
);

CREATE TABLE model_relationship_snapshot (
    id uuid NOT NULL,
    lineage_id uuid NOT NULL,
    model_snapshot_id uuid NOT NULL,
    key text NOT NULL,
    name text,
    description text
);

CREATE TABLE model_relationship_tag_snapshot (
    model_relationship_snapshot_id uuid NOT NULL,
    tag_id uuid NOT NULL
);

CREATE TABLE model_search_item_snapshot (
    id uuid NOT NULL,
    item_type text NOT NULL,
    model_snapshot_id uuid NOT NULL,
    model_key text NOT NULL,
    model_label text NOT NULL,
    entity_id uuid,
    entity_key text,
    entity_label text,
    relationship_id uuid,
    relationship_key text,
    relationship_label text,
    attribute_id uuid,
    attribute_key text,
    attribute_label text,
    search_text text NOT NULL
);

CREATE TABLE model_search_item_tag_snapshot (
    search_item_id uuid NOT NULL,
    tag_id uuid NOT NULL
);

CREATE TABLE model_snapshot (
    id uuid NOT NULL,
    model_id uuid NOT NULL,
    key text NOT NULL,
    name text,
    description text,
    origin text NOT NULL,
    authority text NOT NULL,
    documentation_home text,
    snapshot_kind text NOT NULL,
    up_to_revision integer NOT NULL,
    model_event_release_id uuid,
    version text NOT NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL
);

CREATE TABLE model_tag_snapshot (
    model_snapshot_id uuid NOT NULL,
    tag_id uuid NOT NULL
);

CREATE TABLE model_type_snapshot (
    id uuid NOT NULL,
    lineage_id uuid NOT NULL,
    model_snapshot_id uuid NOT NULL,
    key text NOT NULL,
    name text,
    description text
);

ALTER TABLE ONLY model_business_key_attribute_snapshot
ADD CONSTRAINT model_business_key_attribute__model_business_key_snapshot__key1 UNIQUE (model_business_key_snapshot_id, priority);

ALTER TABLE ONLY model_business_key_attribute_snapshot
ADD CONSTRAINT model_business_key_attribute__model_business_key_snapshot_i_key UNIQUE (
    model_business_key_snapshot_id, model_entity_attribute_snapshot_id
);

ALTER TABLE ONLY model_business_key_snapshot
ADD CONSTRAINT model_business_key_snapshot_model_entity_snapshot_id_key_key UNIQUE (model_entity_snapshot_id, key);

ALTER TABLE ONLY model_business_key_snapshot
ADD CONSTRAINT model_business_key_snapshot_pkey PRIMARY KEY (id);

ALTER TABLE ONLY model_entity_attribute_snapshot
ADD CONSTRAINT model_entity_attribute_snapsh_model_entity_snapshot_id_line_key UNIQUE (model_entity_snapshot_id, lineage_id);

ALTER TABLE ONLY model_entity_attribute_snapshot
ADD CONSTRAINT model_entity_attribute_snapsho_model_entity_snapshot_id_key_key UNIQUE (model_entity_snapshot_id, key);

ALTER TABLE ONLY model_entity_attribute_snapshot
ADD CONSTRAINT model_entity_attribute_snapshot_pkey PRIMARY KEY (id);

ALTER TABLE ONLY model_entity_attribute_tag_snapshot
ADD CONSTRAINT model_entity_attribute_tag_snapshot_pkey PRIMARY KEY (model_entity_attribute_snapshot_id, tag_id);

ALTER TABLE ONLY model_entity_pk_attribute_snapshot
ADD CONSTRAINT model_entity_pk_attribute_sna_model_entity_pk_snapshot_id_m_key UNIQUE (
    model_entity_pk_snapshot_id, model_entity_attribute_snapshot_id
);

ALTER TABLE ONLY model_entity_pk_attribute_snapshot
ADD CONSTRAINT model_entity_pk_attribute_sna_model_entity_pk_snapshot_id_p_key UNIQUE (model_entity_pk_snapshot_id, priority);

ALTER TABLE ONLY model_entity_pk_snapshot
ADD CONSTRAINT model_entity_pk_snapshot_pkey PRIMARY KEY (id);

ALTER TABLE ONLY model_entity_snapshot
ADD CONSTRAINT model_entity_snapshot_model_snapshot_id_key_key UNIQUE (model_snapshot_id, key);

ALTER TABLE ONLY model_entity_snapshot
ADD CONSTRAINT model_entity_snapshot_model_snapshot_id_lineage_id_key UNIQUE (model_snapshot_id, lineage_id);

ALTER TABLE ONLY model_entity_snapshot
ADD CONSTRAINT model_entity_snapshot_pkey PRIMARY KEY (id);

ALTER TABLE ONLY model_entity_tag_snapshot
ADD CONSTRAINT model_entity_tag_snapshot_pkey PRIMARY KEY (model_entity_snapshot_id, tag_id);

ALTER TABLE ONLY model_event
ADD CONSTRAINT model_event_model_id_stream_revision_key UNIQUE (model_id, stream_revision);

ALTER TABLE ONLY model_event
ADD CONSTRAINT model_event_pkey PRIMARY KEY (id);

ALTER TABLE ONLY model
ADD CONSTRAINT model_pkey PRIMARY KEY (id);

ALTER TABLE ONLY model_relationship_attribute_snapshot
ADD CONSTRAINT model_relationship_attribute__model_relationship_snapshot__key1 UNIQUE (model_relationship_snapshot_id, key);

ALTER TABLE ONLY model_relationship_attribute_snapshot
ADD CONSTRAINT model_relationship_attribute__model_relationship_snapshot_i_key UNIQUE (model_relationship_snapshot_id, lineage_id);

ALTER TABLE ONLY model_relationship_attribute_snapshot
ADD CONSTRAINT model_relationship_attribute_snapshot_pkey PRIMARY KEY (id);

ALTER TABLE ONLY model_relationship_attribute_tag_snapshot
ADD CONSTRAINT model_relationship_attribute_tag_snapshot_pkey PRIMARY KEY (model_relationship_attribute_snapshot_id, tag_id);

ALTER TABLE ONLY model_relationship_role_snapshot
ADD CONSTRAINT model_relationship_role_snaps_model_relationship_snapshot__key1 UNIQUE (model_relationship_snapshot_id, key);

ALTER TABLE ONLY model_relationship_role_snapshot
ADD CONSTRAINT model_relationship_role_snaps_model_relationship_snapshot_i_key UNIQUE (model_relationship_snapshot_id, lineage_id);

ALTER TABLE ONLY model_relationship_role_snapshot
ADD CONSTRAINT model_relationship_role_snapshot_pkey PRIMARY KEY (id);

ALTER TABLE ONLY model_relationship_snapshot
ADD CONSTRAINT model_relationship_snapshot_model_snapshot_id_key_key UNIQUE (model_snapshot_id, key);

ALTER TABLE ONLY model_relationship_snapshot
ADD CONSTRAINT model_relationship_snapshot_model_snapshot_id_lineage_id_key UNIQUE (model_snapshot_id, lineage_id);

ALTER TABLE ONLY model_relationship_snapshot
ADD CONSTRAINT model_relationship_snapshot_pkey PRIMARY KEY (id);

ALTER TABLE ONLY model_relationship_tag_snapshot
ADD CONSTRAINT model_relationship_tag_snapshot_pkey PRIMARY KEY (model_relationship_snapshot_id, tag_id);

ALTER TABLE ONLY model_search_item_snapshot
ADD CONSTRAINT model_search_item_snapshot_pkey PRIMARY KEY (id);

ALTER TABLE ONLY model_search_item_tag_snapshot
ADD CONSTRAINT model_search_item_tag_snapshot_pkey PRIMARY KEY (search_item_id, tag_id);

ALTER TABLE ONLY model_snapshot
ADD CONSTRAINT model_snapshot_pkey PRIMARY KEY (id);

ALTER TABLE ONLY model_tag_snapshot
ADD CONSTRAINT model_tag_snapshot_pkey PRIMARY KEY (model_snapshot_id, tag_id);

ALTER TABLE ONLY model_type_snapshot
ADD CONSTRAINT model_type_snapshot_model_snapshot_id_key_key UNIQUE (model_snapshot_id, key);

ALTER TABLE ONLY model_type_snapshot
ADD CONSTRAINT model_type_snapshot_model_snapshot_id_lineage_id_key UNIQUE (model_snapshot_id, lineage_id);

ALTER TABLE ONLY model_type_snapshot
ADD CONSTRAINT model_type_snapshot_pkey PRIMARY KEY (id);

CREATE INDEX idx_model_event_model_id ON model_event USING btree (model_id);

CREATE INDEX idx_model_type_model_id ON model_type_snapshot USING btree (model_snapshot_id);

CREATE UNIQUE INDEX ux_model_event_release_model_version ON model_event USING btree (model_id, model_version) WHERE (
    (event_type = 'model_release'::text) AND (model_version IS NOT NULL)
);

CREATE UNIQUE INDEX ux_model_snapshot_current_head_key ON model_snapshot USING btree (key) WHERE (snapshot_kind = 'CURRENT_HEAD'::text);

CREATE UNIQUE INDEX ux_model_snapshot_current_head_model_id ON model_snapshot USING btree (model_id) WHERE (
    snapshot_kind = 'CURRENT_HEAD'::text
);

CREATE UNIQUE INDEX ux_model_snapshot_version_snapshot_release_event_id ON model_snapshot USING btree (model_event_release_id) WHERE (
    (snapshot_kind = 'VERSION_SNAPSHOT'::text) AND (model_event_release_id IS NOT NULL)
);

ALTER TABLE ONLY model_business_key_attribute_snapshot
ADD CONSTRAINT model_business_key_attribute__model_business_key_snapshot__fkey FOREIGN KEY (
    model_business_key_snapshot_id
) REFERENCES model_business_key_snapshot (id) ON DELETE CASCADE;

ALTER TABLE ONLY model_business_key_attribute_snapshot
ADD CONSTRAINT model_business_key_attribute__model_entity_attribute_snaps_fkey FOREIGN KEY (
    model_entity_attribute_snapshot_id
) REFERENCES model_entity_attribute_snapshot (id) ON DELETE CASCADE;

ALTER TABLE ONLY model_business_key_snapshot
ADD CONSTRAINT model_business_key_snapshot_model_entity_snapshot_id_fkey FOREIGN KEY (
    model_entity_snapshot_id
) REFERENCES model_entity_snapshot (id) ON DELETE CASCADE;

ALTER TABLE ONLY model_entity_attribute_snapshot
ADD CONSTRAINT model_entity_attribute_snapshot_model_entity_snapshot_id_fkey FOREIGN KEY (
    model_entity_snapshot_id
) REFERENCES model_entity_snapshot (id) ON DELETE CASCADE;

ALTER TABLE ONLY model_entity_attribute_snapshot
ADD CONSTRAINT model_entity_attribute_snapshot_model_type_snapshot_id_fkey FOREIGN KEY (
    model_type_snapshot_id
) REFERENCES model_type_snapshot (id) ON DELETE CASCADE;

ALTER TABLE ONLY model_entity_attribute_tag_snapshot
ADD CONSTRAINT model_entity_attribute_tag_sn_model_entity_attribute_snaps_fkey FOREIGN KEY (
    model_entity_attribute_snapshot_id
) REFERENCES model_entity_attribute_snapshot (id) ON DELETE CASCADE;

ALTER TABLE ONLY model_entity_pk_attribute_snapshot
ADD CONSTRAINT model_entity_pk_attribute_sna_model_entity_attribute_snaps_fkey FOREIGN KEY (
    model_entity_attribute_snapshot_id
) REFERENCES model_entity_attribute_snapshot (id) ON DELETE CASCADE;

ALTER TABLE ONLY model_entity_pk_attribute_snapshot
ADD CONSTRAINT model_entity_pk_attribute_snap_model_entity_pk_snapshot_id_fkey FOREIGN KEY (
    model_entity_pk_snapshot_id
) REFERENCES model_entity_pk_snapshot (id) ON DELETE CASCADE;

ALTER TABLE ONLY model_entity_pk_snapshot
ADD CONSTRAINT model_entity_pk_snapshot_model_entity_snapshot_id_fkey FOREIGN KEY (
    model_entity_snapshot_id
) REFERENCES model_entity_snapshot (id) ON DELETE CASCADE;

ALTER TABLE ONLY model_entity_snapshot
ADD CONSTRAINT model_entity_snapshot_model_snapshot_id_fkey FOREIGN KEY (model_snapshot_id) REFERENCES model_snapshot (
    id
) ON DELETE CASCADE;

ALTER TABLE ONLY model_entity_tag_snapshot
ADD CONSTRAINT model_entity_tag_snapshot_model_entity_snapshot_id_fkey FOREIGN KEY (
    model_entity_snapshot_id
) REFERENCES model_entity_snapshot (id) ON DELETE CASCADE;

ALTER TABLE ONLY model_event
ADD CONSTRAINT model_event_model_id_fkey FOREIGN KEY (model_id) REFERENCES model (id) ON DELETE CASCADE;

ALTER TABLE ONLY model_relationship_attribute_tag_snapshot
ADD CONSTRAINT model_relationship_attribute__model_relationship_attribute_fkey FOREIGN KEY (
    model_relationship_attribute_snapshot_id
) REFERENCES model_relationship_attribute_snapshot (id) ON DELETE CASCADE;

ALTER TABLE ONLY model_relationship_attribute_snapshot
ADD CONSTRAINT model_relationship_attribute__model_relationship_snapshot__fkey FOREIGN KEY (
    model_relationship_snapshot_id
) REFERENCES model_relationship_snapshot (id) ON DELETE CASCADE;

ALTER TABLE ONLY model_relationship_attribute_snapshot
ADD CONSTRAINT model_relationship_attribute_snapsh_model_type_snapshot_id_fkey FOREIGN KEY (
    model_type_snapshot_id
) REFERENCES model_type_snapshot (id) ON DELETE CASCADE;

ALTER TABLE ONLY model_relationship_role_snapshot
ADD CONSTRAINT model_relationship_role_snaps_model_relationship_snapshot__fkey FOREIGN KEY (
    model_relationship_snapshot_id
) REFERENCES model_relationship_snapshot (id) ON DELETE CASCADE;

ALTER TABLE ONLY model_relationship_role_snapshot
ADD CONSTRAINT model_relationship_role_snapshot_model_entity_snapshot_id_fkey FOREIGN KEY (
    model_entity_snapshot_id
) REFERENCES model_entity_snapshot (id) ON DELETE CASCADE;

ALTER TABLE ONLY model_relationship_snapshot
ADD CONSTRAINT model_relationship_snapshot_model_snapshot_id_fkey FOREIGN KEY (model_snapshot_id) REFERENCES model_snapshot (
    id
) ON DELETE CASCADE;

ALTER TABLE ONLY model_relationship_tag_snapshot
ADD CONSTRAINT model_relationship_tag_snapsh_model_relationship_snapshot__fkey FOREIGN KEY (
    model_relationship_snapshot_id
) REFERENCES model_relationship_snapshot (id) ON DELETE CASCADE;

ALTER TABLE ONLY model_search_item_snapshot
ADD CONSTRAINT model_search_item_snapshot_model_snapshot_id_fkey FOREIGN KEY (model_snapshot_id) REFERENCES model_snapshot (
    id
) ON DELETE CASCADE;

ALTER TABLE ONLY model_search_item_tag_snapshot
ADD CONSTRAINT model_search_item_tag_snapshot_search_item_id_fkey FOREIGN KEY (search_item_id) REFERENCES model_search_item_snapshot (
    id
) ON DELETE CASCADE;

ALTER TABLE ONLY model_snapshot
ADD CONSTRAINT model_snapshot_model_event_release_id_fkey FOREIGN KEY (model_event_release_id) REFERENCES model_event (
    id
) ON DELETE CASCADE;

ALTER TABLE ONLY model_snapshot
ADD CONSTRAINT model_snapshot_model_id_fkey FOREIGN KEY (model_id) REFERENCES model (id) ON DELETE CASCADE;

ALTER TABLE ONLY model_tag_snapshot
ADD CONSTRAINT model_tag_snapshot_model_snapshot_id_fkey FOREIGN KEY (model_snapshot_id) REFERENCES model_snapshot (id) ON DELETE CASCADE;

ALTER TABLE ONLY model_type_snapshot
ADD CONSTRAINT model_type_snapshot_model_snapshot_id_fkey FOREIGN KEY (model_snapshot_id) REFERENCES model_snapshot (id) ON DELETE CASCADE;
