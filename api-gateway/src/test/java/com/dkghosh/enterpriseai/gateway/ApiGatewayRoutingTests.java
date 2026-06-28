package com.dkghosh.enterpriseai.gateway;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ApiGatewayRoutingTests {

    private static final DisposableServer KNOWLEDGE_SERVICE = HttpServer.create()
            .host("localhost")
            .port(0)
            .route(routes -> routes.get(
                    "/api/v1/platform/info",
                    (request, response) -> response
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .sendString(Mono.just("""
                                    {
                                      "service": "knowledge-service",
                                      "platform": "Enterprise AI Knowledge Platform",
                                      "status": "UP"
                                    }
                                    """))
            ))
            .bindNow();

    @Autowired
    private WebTestClient webTestClient;

    @DynamicPropertySource
    static void configureKnowledgeServiceUrl(DynamicPropertyRegistry registry) {
        registry.add(
                "app.services.knowledge-url",
                () -> "http://localhost:" + KNOWLEDGE_SERVICE.port()
        );
    }

    @AfterAll
    static void stopKnowledgeService() {
        KNOWLEDGE_SERVICE.disposeNow();
    }

    @Test
    void routesKnowledgeRequestsAndRemovesPublicPrefix() {
        webTestClient.get()
                .uri("/knowledge/api/v1/platform/info")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.service").isEqualTo("knowledge-service")
                .jsonPath("$.platform").isEqualTo("Enterprise AI Knowledge Platform")
                .jsonPath("$.status").isEqualTo("UP");
    }

    @Test
    void returnsNotFoundForUnknownRoute() {
        webTestClient.get()
                .uri("/unknown")
                .exchange()
                .expectStatus().isNotFound();
    }
}
