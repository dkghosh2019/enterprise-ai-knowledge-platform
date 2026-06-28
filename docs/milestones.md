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

## Planned milestones

### Milestone 2 - Identity and Security

- Spring Security
- OAuth2 resource server
- JWT validation
- Role-based authorization

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
