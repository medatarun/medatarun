ALTER TABLE model_event RENAME COLUMN action_id TO traceability_origin;
UPDATE model_event
SET traceability_origin = 'action:' || traceability_origin
WHERE substr(traceability_origin, 1, 7) <> 'action:';
