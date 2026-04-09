UPDATE auth_actor
SET issuer = 'urn:medatarun:internal'
WHERE issuer LIKE 'urn:medatarun:%'
  AND issuer <> 'urn:medatarun:internal';
