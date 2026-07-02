package com.dkghosh.enterpriseai.knowledge.service;

import com.dkghosh.enterpriseai.knowledge.dto.PlatformInfoResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformInfoServiceTest {

    private final PlatformInfoService platformInfoService =
            new PlatformInfoService();

    @Test
    void returnsPlatformInformation() {
        PlatformInfoResponse response =
                platformInfoService.getPlatformInfo();

        assertThat(response.service()).isEqualTo("knowledge-service");
        assertThat(response.platform())
                .isEqualTo("Enterprise AI Knowledge Platform");
        assertThat(response.status()).isEqualTo("UP");
    }
}