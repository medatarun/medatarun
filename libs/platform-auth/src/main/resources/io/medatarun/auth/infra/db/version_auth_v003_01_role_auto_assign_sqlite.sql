ALTER TABLE auth_role ADD COLUMN auto_assign INTEGER NOT NULL DEFAULT 0;

CREATE UNIQUE INDEX idx_auth_role_auto_assign ON auth_role (auto_assign) WHERE auto_assign = 1;
