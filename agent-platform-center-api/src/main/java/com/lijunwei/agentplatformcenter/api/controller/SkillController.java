package com.lijunwei.agentplatformcenter.api.controller;

import com.lijunwei.agentplatformcenter.api.model.CreatePlatformResourceRequest;
import com.lijunwei.agentplatformcenter.api.model.PlatformResource;
import com.lijunwei.agentplatformcenter.api.service.AgentCatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/skills")
public class SkillController {
    private final AgentCatalogService agentCatalogService;

    public SkillController(AgentCatalogService agentCatalogService) {
        this.agentCatalogService = agentCatalogService;
    }

    @GetMapping
    public List<PlatformResource> listSkills() {
        return agentCatalogService.listSkills();
    }

    @PostMapping
    public PlatformResource createSkill(@Valid @RequestBody CreatePlatformResourceRequest request) {
        return agentCatalogService.createResource("skill", request);
    }
}
