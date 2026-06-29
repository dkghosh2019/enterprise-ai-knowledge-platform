# Local Development

## Prerequisites

- Java 21
- Maven 3.6.3 or later
- IntelliJ IDEA or another Java IDE

## Automated tests

From the repository root:

```bash
mvn clean test
```

The suite verifies all application contexts, gateway forwarding, public-prefix
removal, unknown-route handling, Identity Service OAuth2 behavior, and real
RSA-signed JWT validation at the Gateway.

## Start the applications

Start the Knowledge Service:

```bash
mvn -pl knowledge-service spring-boot:run
```

In another terminal, start the API Gateway:

```bash
mvn -pl api-gateway spring-boot:run
```

In a third terminal, start the Identity Service:

```bash
mvn -pl identity-service spring-boot:run
```

## Manual verification

```text
GET http://localhost:8081/api/v1/platform/info
GET http://localhost:8080/knowledge/api/v1/platform/info
GET http://localhost:8080/actuator/health
GET http://localhost:8081/actuator/health
GET http://localhost:9000/actuator/health
GET http://localhost:9000/.well-known/oauth-authorization-server
GET http://localhost:9000/oauth2/jwks
GET http://localhost:8080/unknown
```

The unknown route must return HTTP `404`. Browsers may render Spring Boot's HTML fallback page because they request HTML; the status code is the acceptance criterion.

## Override the downstream URL

Git Bash:

```bash
export KNOWLEDGE_SERVICE_URL=http://another-host:8081
```

PowerShell:

```powershell
$env:KNOWLEDGE_SERVICE_URL="http://another-host:8081"
```

## Request a development access token

The local client ID is `platform-client`. The default development secret is
`platform-secret`.

```bash
curl -u platform-client:platform-secret \
  -X POST http://localhost:9000/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials&scope=knowledge.read"
```

The response contains an RSA-signed Bearer JWT with the `knowledge.read` scope
and a lifetime of 900 seconds.

Git Bash can request and extract the token into a variable:

```bash
TOKEN=$(curl -s \
  -u platform-client:platform-secret \
  -X POST http://localhost:9000/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials&scope=knowledge.read" \
  | python -c 'import json,sys; print(json.load(sys.stdin)["access_token"])')
```

Call the protected Gateway route:

```bash
curl -i \
  -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/knowledge/api/v1/platform/info
```

Expected result: HTTP `200` and Knowledge Service information JSON.

Verify the failure paths:

```bash
# Missing token: HTTP 401
curl -i http://localhost:8080/knowledge/api/v1/platform/info

# Malformed token: HTTP 401
curl -i \
  -H "Authorization: Bearer invalid-token" \
  http://localhost:8080/knowledge/api/v1/platform/info

# Unknown route: HTTP 404
curl -i http://localhost:8080/unknown
```

Override the development secret before starting the Identity Service:

Git Bash:

```bash
export PLATFORM_CLIENT_SECRET='{noop}replace-this-secret'
```

PowerShell:

```powershell
$env:PLATFORM_CLIENT_SECRET="{noop}replace-this-secret"
```

The `{noop}` prefix is accepted for local development only. Production
credentials must use secure secret storage and an appropriate password encoder.

## Override the Gateway identity configuration

Git Bash:

```bash
export IDENTITY_ISSUER=http://identity-service:9000
export IDENTITY_JWK_SET_URI=http://identity-service:9000/oauth2/jwks
```

PowerShell:

```powershell
$env:IDENTITY_ISSUER="http://identity-service:9000"
$env:IDENTITY_JWK_SET_URI="http://identity-service:9000/oauth2/jwks"
```
