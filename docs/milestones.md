# Milestones

## Milestone 1 - Platform Foundation

### Completed

- [x] Maven multi-module parent
- [x] Knowledge Service on port `8081`
- [x] Layered controller, service, and DTO structure
- [x] Knowledge Service information and health endpoints
- [x] API Gateway on port `8080`
- [x] Static route to Knowledge Service
- [x] Public-prefix removal with `StripPrefix=1`
- [x] Configurable Knowledge Service URL
- [x] Gateway health endpoint
- [x] Context, forwarding, and unknown-route tests

### Acceptance criteria

- [x] Full Maven test suite passes
- [x] Direct Knowledge Service request succeeds
- [x] Gateway-routed request succeeds
- [x] Both health endpoints report `UP`
- [x] Unknown Gateway route returns HTTP `404`

### Milestone 2 - Identity and Security

#### Identity Service foundation completed on feature branch

- [x] Identity Service module on port `9000`
- [x] Spring Authorization Server configuration
- [x] OAuth2 client-credentials grant
- [x] Configurable machine-client secret
- [x] RSA-signed JWT access tokens
- [x] `knowledge.read` scope
- [x] Authorization Server metadata endpoint
- [x] Public JSON Web Key Set endpoint
- [x] Public Actuator health endpoint
- [x] Integration tests for metadata, keys, health, valid tokens, and invalid credentials

#### Gateway JWT security completed on feature branch

- [x] Configure the API Gateway as a reactive OAuth2 Resource Server
- [x] Validate JWT signature, issuer, expiration, and not-before claims
- [x] Require `knowledge.read` for protected Gateway routes
- [x] Preserve public health and unknown-route behavior
- [x] Forward the Bearer token downstream
- [x] Add real RSA/JWK integration tests
- [x] Verify authorized and unauthorized requests end to end

#### Knowledge Service authorization completed on feature branch

- [x] Configure the Knowledge Service as a servlet OAuth2 Resource Server
- [x] Validate JWTs using configurable issuer and JWK Set locations
- [x] Use stateless sessions and disable CSRF for the REST API
- [x] Require `knowledge.read` for protected platform endpoints
- [x] Preserve public health and information endpoints
- [x] Deny unmatched application routes
- [x] Add MockMvc security integration tests
- [x] Add a pure unit test for `PlatformInfoService`
- [x] Verify defense-in-depth authorization with a real issued token
- [x] Run the complete 22-test platform suite successfully

#### Remaining

- [ ] Merge Knowledge Service authorization into `develop`
- [ ] Merge Milestone 2 from `develop` into `main`

## Planned milestones

### Milestone 3 - Knowledge Persistence

- PostgreSQL and database migrations
- JPA entities and repositories
- Validation and centralized exception handling
- Testcontainers integration tests

### Milestone 4 - AI and Retrieval

- Spring AI or LangChain4j
- Embeddings and PGVector
- Retrieval-augmented generation
- Java-native tool calling

### Milestone 5 - Event-Driven Processing

- Apache Kafka
- Reliable producers and consumers
- Idempotency, retries, and the outbox pattern

### Milestone 6 - Reliability and Observability

- Resilience4j
- Metrics and dashboards
- Distributed tracing
- Structured logging and correlation IDs

### Milestone 7 - Delivery

- Containerization
- CI/CD
- Cloud deployment
- Load and performance testing
