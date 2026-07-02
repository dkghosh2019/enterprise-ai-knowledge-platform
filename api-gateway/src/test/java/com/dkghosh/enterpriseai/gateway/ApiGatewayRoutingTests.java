package com.dkghosh.enterpriseai.gateway;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
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

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ApiGatewayRoutingTests {

    private static final String ISSUER = "https://identity.test";
    private static final RSAKey SIGNING_KEY = generateRsaKey("gateway-test-key");
    private static final AtomicReference<String> FORWARDED_AUTHORIZATION = new AtomicReference<>();

    private static final DisposableServer TEST_SERVER = HttpServer.create()
            .host("localhost")
            .port(0)
            .route(routes -> routes
                    .get(
                            "/oauth2/jwks",
                            (request, response) -> response
                                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                    .sendString(Mono.just(
                                            new JWKSet(SIGNING_KEY.toPublicJWK()).toString()
                                    ))
                    )
                    .get(
                            "/api/v1/platform/info",
                            (request, response) -> {
                                FORWARDED_AUTHORIZATION.set(
                                        request.requestHeaders().get(HttpHeaders.AUTHORIZATION)
                                );
                                return response
                                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                        .sendString(Mono.just("""
                                                {
                                                  "service": "knowledge-service",
                                                  "platform": "Enterprise AI Knowledge Platform",
                                                  "status": "UP"
                                                }
                                                """));
                            }
                    ))
            .bindNow();

    @Autowired
    private WebTestClient webTestClient;

    @DynamicPropertySource
    static void configureTestServices(DynamicPropertyRegistry registry) {
        registry.add(
                "app.services.knowledge-url",
                () -> "http://localhost:" + TEST_SERVER.port()
        );
        registry.add(
                "spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> ISSUER
        );
        registry.add(
                "spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
                () -> "http://localhost:" + TEST_SERVER.port() + "/oauth2/jwks"
        );
    }

    @AfterAll
    static void stopTestServer() {
        TEST_SERVER.disposeNow();
    }

    @BeforeEach
    void clearCapturedAuthorizationHeader() {
        FORWARDED_AUTHORIZATION.set(null);
    }

    @Test
    void routesAuthorizedKnowledgeRequestAndForwardsBearerToken() throws Exception {
        String accessToken = createToken(
                SIGNING_KEY,
                ISSUER,
                Instant.now().minusSeconds(5),
                Instant.now().plusSeconds(300),
                "knowledge.read"
        );

        webTestClient.get()
                .uri("/knowledge/api/v1/platform/info")
                .headers(headers -> headers.setBearerAuth(accessToken))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.service").isEqualTo("knowledge-service")
                .jsonPath("$.platform").isEqualTo("Enterprise AI Knowledge Platform")
                .jsonPath("$.status").isEqualTo("UP");

        assertThat(FORWARDED_AUTHORIZATION.get())
                .isEqualTo("Bearer " + accessToken);
    }

    @Test
    void rejectsKnowledgeRequestWithoutBearerToken() {
        webTestClient.get()
                .uri("/knowledge/api/v1/platform/info")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void rejectsTokenWithoutKnowledgeReadScope() throws Exception {
        String accessToken = createToken(
                SIGNING_KEY,
                ISSUER,
                Instant.now().minusSeconds(5),
                Instant.now().plusSeconds(300),
                "profile.read"
        );

        webTestClient.get()
                .uri("/knowledge/api/v1/platform/info")
                .headers(headers -> headers.setBearerAuth(accessToken))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void rejectsTokenFromWrongIssuer() throws Exception {
        String accessToken = createToken(
                SIGNING_KEY,
                "https://untrusted-issuer.test",
                Instant.now().minusSeconds(5),
                Instant.now().plusSeconds(300),
                "knowledge.read"
        );

        webTestClient.get()
                .uri("/knowledge/api/v1/platform/info")
                .headers(headers -> headers.setBearerAuth(accessToken))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void rejectsExpiredToken() throws Exception {
        String accessToken = createToken(
                SIGNING_KEY,
                ISSUER,
                Instant.now().minusSeconds(600),
                Instant.now().minusSeconds(300),
                "knowledge.read"
        );

        webTestClient.get()
                .uri("/knowledge/api/v1/platform/info")
                .headers(headers -> headers.setBearerAuth(accessToken))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void rejectsTokenWithInvalidSignature() throws Exception {
        RSAKey untrustedKey = generateRsaKey(SIGNING_KEY.getKeyID());
        String accessToken = createToken(
                untrustedKey,
                ISSUER,
                Instant.now().minusSeconds(5),
                Instant.now().plusSeconds(300),
                "knowledge.read"
        );

        webTestClient.get()
                .uri("/knowledge/api/v1/platform/info")
                .headers(headers -> headers.setBearerAuth(accessToken))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void permitsHealthRequestWithoutBearerToken() {
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP");
    }

    @Test
    void returnsNotFoundForUnknownRoute() {
        webTestClient.get()
                .uri("/unknown")
                .exchange()
                .expectStatus().isNotFound();
    }

    private static RSAKey generateRsaKey(String keyId) {
        try {
            return new RSAKeyGenerator(2048)
                    .keyID(keyId)
                    .generate();
        } catch (Exception exception) {
            throw new IllegalStateException("Could not generate test RSA key", exception);
        }
    }

    private static String createToken(
            RSAKey signingKey,
            String issuer,
            Instant issuedAt,
            Instant expiresAt,
            String... scopes
    ) throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .subject("platform-client")
                .audience("platform-client")
                .issueTime(Date.from(issuedAt))
                .notBeforeTime(Date.from(issuedAt))
                .expirationTime(Date.from(expiresAt))
                .claim("scope", List.of(scopes))
                .build();

        SignedJWT signedJwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256)
                        .keyID(signingKey.getKeyID())
                        .build(),
                claims
        );
        signedJwt.sign(new RSASSASigner(signingKey.toPrivateKey()));
        return signedJwt.serialize();
    }
}
