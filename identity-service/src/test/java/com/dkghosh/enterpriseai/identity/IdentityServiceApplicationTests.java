package com.dkghosh.enterpriseai.identity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class IdentityServiceApplicationTests {

    private static final String CLIENT_ID = "platform-client";
    private static final String CLIENT_SECRET = "platform-secret";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void contextLoads() {
        assertThat(mockMvc).isNotNull();
    }

    @Test
    void exposesAuthorizationServerMetadata() throws Exception {
        mockMvc.perform(get("/.well-known/oauth-authorization-server"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.issuer").value("http://localhost:9000"))
                .andExpect(jsonPath("$.token_endpoint").value("http://localhost:9000/oauth2/token"))
                .andExpect(jsonPath("$.jwks_uri").value("http://localhost:9000/oauth2/jwks"));
    }

    @Test
    void exposesPublicSigningKey() throws Exception {
        mockMvc.perform(get("/oauth2/jwks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keys[0].kty").value("RSA"))
                .andExpect(jsonPath("$.keys[0].kid").isNotEmpty())
                .andExpect(jsonPath("$.keys[0].n").isNotEmpty())
                .andExpect(jsonPath("$.keys[0].e").isNotEmpty());
    }

    @Test
    void reportsHealthy() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void issuesJwtForValidClientCredentials() throws Exception {
        String responseBody = mockMvc.perform(post("/oauth2/token")
                        .with(httpBasic(CLIENT_ID, CLIENT_SECRET))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "client_credentials")
                        .param("scope", "knowledge.read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isNotEmpty())
                .andExpect(jsonPath("$.token_type").value("Bearer"))
                .andExpect(jsonPath("$.scope").value("knowledge.read"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode tokenResponse = objectMapper.readTree(responseBody);
        JsonNode claims = decodeClaims(tokenResponse.get("access_token").asText());

        assertThat(claims.get("iss").asText()).isEqualTo("http://localhost:9000");
        assertThat(claims.get("sub").asText()).isEqualTo(CLIENT_ID);
        assertThat(claims.get("scope"))
                .extracting(JsonNode::asText)
                .containsExactly("knowledge.read");
    }

    @Test
    void rejectsInvalidClientSecret() throws Exception {
        mockMvc.perform(post("/oauth2/token")
                        .with(httpBasic(CLIENT_ID, "wrong-secret"))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "client_credentials"))
                .andExpect(status().isUnauthorized());
    }

    private JsonNode decodeClaims(String jwt) throws Exception {
        String[] tokenParts = jwt.split("\\.");
        assertThat(tokenParts).hasSize(3);

        byte[] decodedClaims = Base64.getUrlDecoder().decode(tokenParts[1]);
        return objectMapper.readTree(new String(decodedClaims, StandardCharsets.UTF_8));
    }
}
