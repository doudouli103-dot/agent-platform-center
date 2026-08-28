package com.lijunwei.agentplatformcenter.api.controller;

import com.lijunwei.agentplatformcenter.api.model.AgentDefinition;
import com.lijunwei.agentplatformcenter.api.model.CreateAgentRequest;
import com.lijunwei.agentplatformcenter.api.service.AgentCatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/agents")
public class AgentController {
    private final AgentCatalogService agentCatalogService;

    public AgentController(AgentCatalogService agentCatalogService) {
        this.agentCatalogService = agentCatalogService;
    }

    @GetMapping
    public List<AgentDefinition> listAgents() {
        return agentCatalogService.listAgents();
    }

    @PostMapping
    public AgentDefinition createAgent(@Valid @RequestBody CreateAgentRequest request) {
        return agentCatalogService.createAgent(request);
    }
}
