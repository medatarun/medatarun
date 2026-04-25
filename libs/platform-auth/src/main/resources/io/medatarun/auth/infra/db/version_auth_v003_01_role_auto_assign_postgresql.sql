ALTER TABLE auth_role ADD COLUMN auto_assign boolean NOT NULL DEFAULT false;

CREATE UNIQUE INDEX idx_auth_role_auto_assign ON auth_role USING btree (auto_assign) WHERE auto_assign IS TRUE;
