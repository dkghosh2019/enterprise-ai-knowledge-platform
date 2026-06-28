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

The suite verifies both application contexts, gateway forwarding, public-prefix removal, and HTTP `404` handling for an unknown route.

## Start both applications

Start the Knowledge Service:

```bash
mvn -pl knowledge-service spring-boot:run
```

In another terminal, start the API Gateway:

```bash
mvn -pl api-gateway spring-boot:run
```

## Manual verification

```text
GET http://localhost:8081/api/v1/platform/info
GET http://localhost:8080/knowledge/api/v1/platform/info
GET http://localhost:8080/actuator/health
GET http://localhost:8081/actuator/health
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
