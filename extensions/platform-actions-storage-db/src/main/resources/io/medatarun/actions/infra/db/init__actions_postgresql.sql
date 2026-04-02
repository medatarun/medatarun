CREATE TABLE action_audit_event (
    action_instance_id UUID PRIMARY KEY,
    action_group_key TEXT NOT NULL,
    action_key TEXT NOT NULL,
    actor_id UUID,
    source TEXT NOT NULL,
    payload_serialized JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    status TEXT NOT NULL,
    error_code TEXT,
    error_message TEXT
);

CREATE INDEX idx_action_audit_event_action_key ON action_audit_event(action_key);
CREATE INDEX idx_action_audit_event_actor_id ON action_audit_event(actor_id);
CREATE INDEX idx_action_audit_event_created_at ON action_audit_event(created_at);
CREATE INDEX idx_action_audit_event_group_key ON action_audit_event(action_group_key);
CREATE INDEX idx_action_audit_event_status ON action_audit_event(status);
