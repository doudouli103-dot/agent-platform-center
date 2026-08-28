package com.lijunwei.agentplatformcenter.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lijunwei.agentplatformcenter.api.config.GatewayClientProperties;
import com.lijunwei.agentplatformcenter.api.model.AgentDefinition;
import com.lijunwei.agentplatformcenter.api.model.RunRecord;
import com.lijunwei.agentplatformcenter.api.model.RunRequest;
import com.lijunwei.agentplatformcenter.api.model.RunResponse;
import com.lijunwei.agentplatformcenter.api.service.AgentCatalogService;
import com.lijunwei.agentplatformcenter.api.service.RunHistoryService;
import com.lijunwei.agentplatformcenter.api.service.TenxAiGatewayClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

@RestController
@RequestMapping("/api/runs")
public class RunController {
    private final AgentCatalogService catalogService;
    private final TenxAiGatewayClient gatewayClient;
    private final GatewayClientProperties gatewayProperties;
    private final RunHistoryService historyService;
    private final ObjectMapper objectMapper;
    private final Map<String, RunRequest> runRequests = new ConcurrentHashMap<>();

    public RunController(AgentCatalogService catalogService, TenxAiGatewayClient gatewayClient,
                         GatewayClientProperties gatewayProperties, RunHistoryService historyService,
                         ObjectMapper objectMapper) {
        this.catalogService = catalogService;
        this.gatewayClient = gatewayClient;
        this.gatewayProperties = gatewayProperties;
        this.historyService = historyService;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    public RunResponse createRun(@Valid @RequestBody RunRequest request) {
        String runId = "run-" + UUID.randomUUID().toString().substring(0, 8);
        Optional<AgentDefinition> agent = catalogService.findAgent(request.getAgentId());
        historyService.createRun(runId, request, agent);
        runRequests.put(runId, request);
        return new RunResponse(runId, "created", "/api/runs/" + runId + "/events");
    }

    @GetMapping
    public List<RunRecord> listRuns() {
        return historyService.listRuns();
    }

    @GetMapping(path = "/{runId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEvents(@PathVariable String runId) {
        SseEmitter emitter = new SseEmitter(60_000L);

        Thread worker = new Thread(functionForRun(emitter, runId));
        worker.setName("run-events-" + runId);
        worker.start();

        return emitter;
    }

    private Runnable functionForRun(final SseEmitter emitter, final String runId) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    RunRequest request = runRequests.get(runId);
                    Optional<AgentDefinition> agent = request == null ? Optional.empty() : catalogService.findAgent(request.getAgentId());
                    String model = agent.map(AgentDefinition::getModel).orElse("qwen3-coder-next");
                    historyService.markRunning(runId);

                    send(runId, emitter, "run.started", data("runId", runId));
                    send(runId, emitter, "skill.selected", data("skill", firstOrDefault(agent.map(AgentDefinition::getSkills).orElse(null), "skill-java-review@v1")));
                    send(runId, emitter, "mcp.started", data("server", firstOrDefault(agent.map(AgentDefinition::getMcpServers).orElse(null), "mcp-filesystem@v1")));
                    send(runId, emitter, "mcp.completed", data("durationMs", 128));
                    Map<String, Object> gatewayData = gatewaySelection(model);
                    historyService.updateGatewayData(runId, objectMapper.writeValueAsString(gatewayData));
                    send(runId, emitter, "gateway.selected", gatewayData);
                    String reply = modelReply(model, request == null ? "" : request.getMessage());
                    historyService.appendAssistantOutput(runId, reply);
                    send(runId, emitter, "model.token", data("text", reply));
                    Map<String, Object> traceData = traceCompleted();
                    send(runId, emitter, "trace.completed", traceData);
                    historyService.markCompleted(runId, objectMapper.writeValueAsString(traceData));
                    send(runId, emitter, "run.completed", data("status", "completed"));
                    runRequests.remove(runId);
                    emitter.complete();
                } catch (Exception ex) {
                    historyService.markFailed(runId, ex.getMessage());
                    emitter.completeWithError(ex);
                }
            }
        };
    }

    private String modelReply(String model, String message) {
        if (!gatewayProperties.isEnabled()) {
            return "这个 Agent 已经完成首轮分析。当前网关调用未启用，打开 AGENT_CENTER_GATEWAY_ENABLED=true 后会通过 tenx-ai-gateway 调用模型。";
        }
        return gatewayClient.chat(model, message);
    }

    private Map<String, Object> gatewaySelection(String model) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("enabled", gatewayProperties.isEnabled());
        data.put("baseUrl", gatewayProperties.getBaseUrl());
        data.put("model", model);
        return data;
    }

    private Map<String, Object> traceCompleted() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("modelCalls", gatewayProperties.isEnabled() ? 1 : 0);
        data.put("toolCalls", 1);
        data.put("durationMs", 860);
        return data;
    }

    private Object data(String key, Object value) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put(key, value);
        return data;
    }

    private String firstOrDefault(java.util.List<String> values, String defaultValue) {
        if (values == null || values.isEmpty()) {
            return defaultValue;
        }
        return values.get(0);
    }

    private void send(String runId, SseEmitter emitter, String eventName, String data) throws IOException, InterruptedException {
        historyService.recordEvent(runId, eventName, data);
        emitter.send(SseEmitter.event().name(eventName).data(data));
        Thread.sleep(350L);
    }

    private void send(String runId, SseEmitter emitter, String eventName, Object data) throws IOException, InterruptedException {
        send(runId, emitter, eventName, objectMapper.writeValueAsString(data));
    }
}
