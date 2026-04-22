CREATE TABLE tag_event (
    id uuid NOT NULL,
    scope_type text NOT NULL,
    scope_id uuid,
    stream_revision integer NOT NULL,
    event_type text NOT NULL,
    event_version integer NOT NULL,
    actor_id uuid NOT NULL,
    traceability_origin text NOT NULL,
    created_at timestamp with time zone NOT NULL,
    payload jsonb NOT NULL
);

CREATE TABLE tag_view_current_tag (
    id uuid NOT NULL,
    scope_type text NOT NULL,
    scope_id uuid,
    tag_group_id uuid,
    key text NOT NULL,
    name text,
    description text
);

CREATE TABLE tag_view_current_tag_group (
    id uuid NOT NULL,
    key text NOT NULL,
    name text,
    description text
);

CREATE TABLE tag_view_history_tag (
    id uuid NOT NULL,
    tag_event_id uuid NOT NULL,
    tag_id uuid NOT NULL,
    scope_type text NOT NULL,
    scope_id uuid,
    tag_group_id uuid,
    key text NOT NULL,
    name text,
    description text,
    valid_from timestamp with time zone NOT NULL,
    valid_to timestamp with time zone
);

CREATE TABLE tag_view_history_tag_group (
    id uuid NOT NULL,
    tag_event_id uuid NOT NULL,
    tag_group_id uuid NOT NULL,
    key text NOT NULL,
    name text,
    description text,
    valid_from timestamp with time zone NOT NULL,
    valid_to timestamp with time zone
);

ALTER TABLE ONLY tag_event
ADD CONSTRAINT tag_event_pkey PRIMARY KEY (id);

ALTER TABLE ONLY tag_event
ADD CONSTRAINT tag_event_scope_type_scope_id_stream_revision_key UNIQUE (scope_type, scope_id, stream_revision);

ALTER TABLE ONLY tag_view_current_tag_group
ADD CONSTRAINT tag_view_current_tag_group_key_key UNIQUE (key);

ALTER TABLE ONLY tag_view_current_tag_group
ADD CONSTRAINT tag_view_current_tag_group_pkey PRIMARY KEY (id);

ALTER TABLE ONLY tag_view_current_tag
ADD CONSTRAINT tag_view_current_tag_pkey PRIMARY KEY (id);

ALTER TABLE ONLY tag_view_history_tag_group
ADD CONSTRAINT tag_view_history_tag_group_pkey PRIMARY KEY (id);

ALTER TABLE ONLY tag_view_history_tag_group
ADD CONSTRAINT tag_view_history_tag_group_tag_event_id_key UNIQUE (tag_event_id);

ALTER TABLE ONLY tag_view_history_tag
ADD CONSTRAINT tag_view_history_tag_pkey PRIMARY KEY (id);

ALTER TABLE ONLY tag_view_history_tag
ADD CONSTRAINT tag_view_history_tag_tag_event_id_key UNIQUE (tag_event_id);

CREATE INDEX idx_tag_view_current_tag__group_key ON tag_view_current_tag USING btree (tag_group_id, key);

CREATE INDEX idx_tag_view_current_tag__scope_key ON tag_view_current_tag USING btree (scope_type, scope_id, key);

CREATE INDEX idx_tag_view_history_tag__lookup ON tag_view_history_tag USING btree (tag_id, valid_from, valid_to);

CREATE INDEX idx_tag_view_history_tag__scope ON tag_view_history_tag USING btree (scope_type, scope_id);

CREATE INDEX idx_tag_view_history_tag_group__lookup ON tag_view_history_tag_group USING btree (tag_group_id, valid_from, valid_to);

ALTER TABLE ONLY tag_view_current_tag
ADD CONSTRAINT tag_view_current_tag_tag_group_id_fkey FOREIGN KEY (tag_group_id) REFERENCES tag_view_current_tag_group (
    id
) ON DELETE CASCADE;

ALTER TABLE ONLY tag_view_history_tag_group
ADD CONSTRAINT tag_view_history_tag_group_tag_event_id_fkey FOREIGN KEY (tag_event_id) REFERENCES tag_event (id) ON DELETE CASCADE;

ALTER TABLE ONLY tag_view_history_tag
ADD CONSTRAINT tag_view_history_tag_tag_event_id_fkey FOREIGN KEY (tag_event_id) REFERENCES tag_event (id) ON DELETE CASCADE;
