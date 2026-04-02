CREATE TABLE action_audit_event (
    action_instance_id BINARY(16) PRIMARY KEY NOT NULL,
    action_group_key TEXT NOT NULL,
    action_key TEXT NOT NULL,
    actor_id BINARY(16) NULL,
    source TEXT NOT NULL,
    payload_serialized TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    status TEXT NOT NULL,
    error_code TEXT NULL,
    error_message TEXT NULL
);

CREATE INDEX idx_action_audit_event_action_key ON action_audit_event(action_key);
CREATE INDEX idx_action_audit_event_actor_id ON action_audit_event(actor_id);
CREATE INDEX idx_action_audit_event_created_at ON action_audit_event(created_at);
CREATE INDEX idx_action_audit_event_group_key ON action_audit_event(action_group_key);
CREATE INDEX idx_action_audit_event_status ON action_audit_event(status);
