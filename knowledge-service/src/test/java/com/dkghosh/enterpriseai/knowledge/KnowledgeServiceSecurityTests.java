package com.dkghosh.enterpriseai.knowledge;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class KnowledgeServiceSecurityTests {

    private final MockMvc mockMvc;

    @Autowired
    KnowledgeServiceSecurityTests(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }





    @Test
    void permitsHealthRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void rejectsPlatformRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/platform/info"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsMalformedBearerToken() throws Exception {
        mockMvc.perform(get("/api/v1/platform/info")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsTokenWithoutKnowledgeReadScope() throws Exception {
        mockMvc.perform(get("/api/v1/platform/info")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("SCOPE_profile.read")
                        )))
                .andExpect(status().isForbidden());
    }

    @Test
    void permitsTokenWithKnowledgeReadScope() throws Exception {
        mockMvc.perform(get("/api/v1/platform/info")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("SCOPE_knowledge.read")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("knowledge-service"))
                .andExpect(jsonPath("$.platform")
                        .value("Enterprise AI Knowledge Platform"))
                .andExpect(jsonPath("$.status").value("UP"));
    }
}