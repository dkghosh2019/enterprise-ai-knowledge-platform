package com.dkghosh.enterpriseai.knowledge.service;

import com.dkghosh.enterpriseai.knowledge.dto.PlatformInfoResponse;
import org.springframework.stereotype.Service;

@Service
public class PlatformInfoService {

    public PlatformInfoResponse getPlatformInfo() {
        return new PlatformInfoResponse(
                "knowledge-service",
                "Enterprise AI Knowledge Platform",
                "UP"
        );
    }
}
