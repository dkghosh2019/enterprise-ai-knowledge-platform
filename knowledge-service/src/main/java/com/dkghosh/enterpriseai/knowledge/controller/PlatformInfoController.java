package com.dkghosh.enterpriseai.knowledge.controller;

import com.dkghosh.enterpriseai.knowledge.dto.PlatformInfoResponse;
import com.dkghosh.enterpriseai.knowledge.service.PlatformInfoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/platform")
public class PlatformInfoController {

    private final PlatformInfoService platformInfoService;

    public PlatformInfoController(PlatformInfoService platformInfoService) {
        this.platformInfoService = platformInfoService;
    }

    @GetMapping("/info")
    public PlatformInfoResponse getPlatformInfo() {
        return platformInfoService.getPlatformInfo();
    }
}
