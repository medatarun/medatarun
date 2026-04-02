DROP TABLE IF EXISTS action_audit_event_v002;

CREATE TABLE action_audit_event_v002 (
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

INSERT INTO action_audit_event_v002 (
    action_instance_id,
    action_group_key,
    action_key,
    actor_id,
    source,
    payload_serialized,
    created_at,
    status,
    error_code,
    error_message
)
SELECT
    unhex(replace(action_instance_id, '-', '')),
    action_group_key,
    action_key,
    CASE
        WHEN actor_id IS NULL THEN NULL
        ELSE unhex(replace(actor_id, '-', ''))
    END,
    source,
    payload_serialized,
    strftime('%Y-%m-%d %H:%M:%f', created_at / 1000.0, 'unixepoch', 'localtime'),
    status,
    error_code,
    error_message
FROM action_audit_event;

DROP TABLE action_audit_event;
ALTER TABLE action_audit_event_v002 RENAME TO action_audit_event;

CREATE INDEX idx_action_audit_event_group_key ON action_audit_event(action_group_key);
CREATE INDEX idx_action_audit_event_action_key ON action_audit_event(action_key);
CREATE INDEX idx_action_audit_event_actor_id ON action_audit_event(actor_id);
CREATE INDEX idx_action_audit_event_created_at ON action_audit_event(created_at);
CREATE INDEX idx_action_audit_event_status ON action_audit_event(status);
