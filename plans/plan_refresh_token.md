# Plan d'implémentation du refresh token OIDC interne

## Contexte

Medatarun peut utiliser un serveur OIDC externe ou son serveur OIDC interne.

Quand `oidcAuthority` pointe vers un fournisseur externe, le refresh token est
géré par ce fournisseur. Le backend Medatarun continue uniquement à valider les
access tokens émis par cet issuer externe.

Quand `oidcAuthority` pointe vers Medatarun, le serveur interne expose déjà les
endpoints OIDC, mais le token endpoint ne supporte aujourd'hui que
`grant_type=authorization_code`. Le frontend demande déjà `offline_access` et il
est déjà configuré pour rafraîchir les tokens. Le travail à faire est donc côté
backend OIDC interne.

Objectif : éviter une reconnexion login/mot de passe toutes les 60 minutes, tout
en gardant des access tokens courts. Tant que l'utilisateur utilise l'
application, le client doit obtenir de nouveaux tokens via refresh token.

## Phase 1 - Router les grants dans le token endpoint

### Objectif

Le token endpoint HTTP reste unique. Il doit lire `grant_type` et déléguer vers
le traitement métier correspondant.

### Changements

- Adapter le parsing dans
  `app/src/main/kotlin/io/medatarun/httpserver/oidc/AppHttpServerOIDCConfig.kt`.
- Ne plus exiger systématiquement `code`, `redirect_uri` et `code_verifier`.
- Router selon `grant_type` :
    - `authorization_code` : construire `OidcTokenRequest` avec `code`,
      `redirect_uri`, `client_id`, `code_verifier`, puis appeler
      `oidcService.oidcToken(request)`.
    - `refresh_token` : construire `OidcTokenRefreshRequest` avec `client_id`,
      `refresh_token`, puis appeler `oidcService.oidcTokenRefresh(request)`.
- Créer
  `libs/platform-auth/src/main/kotlin/io/medatarun/auth/domain/oidc/OidcTokenRefreshRequest.kt`.
- Ajouter `oidcTokenRefresh(request: OidcTokenRefreshRequest)` dans
  `libs/platform-auth/src/main/kotlin/io/medatarun/auth/ports/exposed/OidcService.kt`.
- Retirer `grantType` des DTOs métier si le routeur HTTP valide déjà le grant
  type.
- Retourner `invalid_request` quand `grant_type` est absent.
- Retourner `unsupported_grant_type` quand `grant_type` contient une valeur non
  supportée.

### Résultat attendu

Une requête `grant_type=refresh_token` arrive au service OIDC au lieu d'être
rejetée par le parsing HTTP. Le workflow `authorization_code` et le workflow
`refresh_token` restent séparés dans le contrat du service.

## Phase 2 - Annoncer le support dans la discovery OIDC

### Objectif

Le document `/.well-known/openid-configuration` doit annoncer les capacités
réellement supportées par le serveur OIDC interne.

### Changements

Dans
`libs/platform-auth/src/main/kotlin/io/medatarun/auth/internal/oidc/OidcServiceImpl.kt`,
modifier `oidcWellKnownOpenIdConfiguration()` :

- Ajouter `"refresh_token"` dans `grant_types_supported`.
- Ajouter `"offline_access"` dans `scopes_supported`.

### Résultat attendu

Les clients OIDC voient que Medatarun supporte le refresh token et le scope
`offline_access`.

## Phase 3 - Étendre la réponse token

### Objectif

Le serveur doit retourner un refresh token au client quand les conditions
d'émission sont remplies.

### Changements

- Ajouter un champ sérialisé `refresh_token` dans
  `libs/platform-auth/src/main/kotlin/io/medatarun/auth/ports/exposed/OIDCTokenResponse.kt`.
- Renseigner ce champ quand un refresh token est émis.
- Garder `access_token`, `id_token`, `token_type` et `expires_in` dans la
  réponse.

### Résultat attendu

Après un login par authorization code, le client reçoit aussi un refresh token
quand il a demandé `offline_access` et que le client OIDC est autorisé à
utiliser ce grant.

## Phase 4 - Ajouter le stockage des refresh tokens

### Objectif

Le serveur doit pouvoir vérifier, expirer, révoquer et faire tourner les refresh
tokens.

### Changements

- Étendre le port
  `libs/platform-auth/src/main/kotlin/io/medatarun/auth/ports/needs/OidcStorage.kt`.
- Ajouter une table de stockage pour SQLite.
- Ajouter la même table de stockage pour PostgreSQL.
- Stocker le hash du refresh token, jamais le token brut.
- Stocker au minimum :
    - hash du token
    - `client_id`
    - `subject`
    - `scope`
    - `auth_time`
    - `created_at`
    - `expires_at`
    - `revoked_at`
- Ajouter `last_used_at` et `replaced_by` pour gérer la rotation.
- Étendre `purgeExpired(now)` pour supprimer aussi les refresh tokens expirés.

### Résultat attendu

Le serveur dispose d'un état persistant pour valider un refresh token même après
expiration de l'access token initial.

## Phase 5 - Émettre le refresh token au login initial

### Objectif

Le serveur doit créer un refresh token pendant l'échange `authorization_code`.

### Conditions d'émission

Émettre un refresh token seulement si :

- le scope autorisé contient `offline_access`;
- le client autorise `refresh_token` dans ses `grantTypes`;
- le client est connu et autorisé;
- l'utilisateur correspond à un acteur Medatarun actif.

### Changements

Dans `OidcServiceImpl.oidcToken(...)`, après validation du code PKCE :

- créer l'access token comme aujourd'hui;
- créer l'id token comme aujourd'hui;
- créer et persister un refresh token;
- retourner le refresh token dans `OIDCTokenResponse`.

### Résultat attendu

Le premier échange token donne au frontend tout ce qu'il faut pour rafraîchir la
session sans nouvelle saisie du mot de passe.

## Phase 6 - Implémenter `oidcTokenRefresh`

### Objectif

Le service OIDC doit émettre de nouveaux tokens à partir d'un refresh token
valide.

### Vérifications

Dans `OidcServiceImpl.oidcTokenRefresh(...)` :

- calculer le hash du refresh token reçu;
- retrouver le refresh token stocké;
- vérifier qu'il n'est pas expiré;
- vérifier qu'il n'est pas révoqué;
- vérifier que le `client_id` correspond;
- retrouver l'acteur par issuer et subject;
- refuser la requête si l'acteur est désactivé ou introuvable.

### Réponse

Retourner :

- un nouvel `access_token`;
- un nouvel `id_token`;
- `token_type=Bearer`;
- `expires_in` selon le TTL access token;
- un nouveau `refresh_token`.

### Résultat attendu

Le frontend renouvelle sa session tant que le refresh token est encore valide.

## Phase 7 - Définir la politique de durée de session

### Objectif

Définir combien de temps une session peut rester active sans login/mot de passe.

### Décisions à prendre

- Garder le TTL actuel de 60 minutes pour les access tokens.
- Créer une nouvelle propriété de configuration standard de l'application pour
  la durée des refresh tokens.
- Donner à cette nouvelle propriété une valeur par défaut de 30 jours.

### Règle

La durée des refresh tokens vient de la configuration standard de l'application.
Elle ne doit pas être déduite du TTL des access tokens et ne doit pas être codée
en dur dans l'implémentation.

## Phase 8 - Implémenter la rotation et la révocation

### Objectif

Limiter l'impact d'un refresh token volé.

### Changements

- À chaque refresh réussi, révoquer l'ancien refresh token.
- Émettre et stocker un nouveau refresh token.
- Retourner toujours le nouveau refresh token au client.
- Refuser un refresh token déjà remplacé.

### Comportement en cas de réutilisation

Si un refresh token déjà remplacé est réutilisé, refuser la requête.
`oidcTokenRefresh(...)` retourne toujours un nouveau refresh token et rend
l'ancien inutilisable.

## Phase 9 - Tests à prévoir après validation

AGENTS.md demande de faire valider les plans de tests avant de les écrire. Cette
phase liste donc les scénarios à valider avant toute implémentation de tests.

Scénarios proposés :

- discovery OIDC annonce `authorization_code` et `refresh_token`;
- discovery OIDC annonce `offline_access`;
- authorization code sans `offline_access` ne retourne pas de refresh token;
- authorization code avec `offline_access` retourne un refresh token;
- `grant_type=refresh_token` retourne de nouveaux tokens avec un refresh token
  valide;
- refresh token expiré retourne une erreur OIDC;
- refresh token révoqué retourne une erreur OIDC;
- refresh token avec mauvais `client_id` retourne une erreur OIDC;
- ancien refresh token refusé après rotation;
- access token émis par refresh reste valide pour les API protégées.

## Ordre d'implémentation

1. Mettre en place le dispatch `grant_type` dans le token endpoint et séparer
   `OidcTokenRequest` / `OidcTokenRefreshRequest`.
2. Ajouter la discovery `refresh_token` et `offline_access`.
3. Ajouter le champ `refresh_token` dans la réponse.
4. Ajouter le stockage persistant.
5. Émettre le refresh token au login initial.
6. Implémenter `oidcService.oidcTokenRefresh(...)`.
7. Ajouter rotation, expiration et purge.
8. Faire valider le plan de tests.
9. Écrire les tests validés.
