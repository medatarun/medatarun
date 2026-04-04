CREATE TABLE auth_role (
  id BINARY(16) PRIMARY KEY UNIQUE,
  name VARCHAR(30) NOT NULL,
  description TEXT,
  created_at TIMESTAMP NOT NULL,
  last_updated_at TIMESTAMP NOT NULL
);

CREATE TABLE auth_role_permission (
  auth_role_id BINARY(16) NOT NULL,
  permission VARCHAR(50) NOT NULL,
  PRIMARY KEY (auth_role_id, permission),
  FOREIGN KEY (auth_role_id) REFERENCES auth_role(id)
);

CREATE TABLE auth_actor_role (
  auth_actor_id BINARY(16) NOT NULL,
  auth_role_id BINARY(16) NOT NULL,
  PRIMARY KEY (auth_actor_id, auth_role_id),
  FOREIGN KEY (auth_actor_id) REFERENCES auth_actor(id),
  FOREIGN KEY (auth_role_id) REFERENCES auth_role(id)
);
