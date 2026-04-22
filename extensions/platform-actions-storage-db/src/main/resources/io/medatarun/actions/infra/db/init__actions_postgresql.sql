CREATE TABLE action_audit_event (
    action_instance_id uuid NOT NULL,
    action_group_key text NOT NULL,
    action_key text NOT NULL,
    actor_id uuid,
    source text NOT NULL,
    payload_serialized jsonb NOT NULL,
    created_at timestamp with time zone NOT NULL,
    status text NOT NULL,
    error_code text,
    error_message text
);

ALTER TABLE ONLY action_audit_event
ADD CONSTRAINT action_audit_event_pkey PRIMARY KEY (action_instance_id);

CREATE INDEX idx_action_audit_event_action_key ON action_audit_event USING btree (action_key);

CREATE INDEX idx_action_audit_event_actor_id ON action_audit_event USING btree (actor_id);

CREATE INDEX idx_action_audit_event_created_at ON action_audit_event USING btree (created_at);

CREATE INDEX idx_action_audit_event_group_key ON action_audit_event USING btree (action_group_key);

CREATE INDEX idx_action_audit_event_status ON action_audit_event USING btree (status);
