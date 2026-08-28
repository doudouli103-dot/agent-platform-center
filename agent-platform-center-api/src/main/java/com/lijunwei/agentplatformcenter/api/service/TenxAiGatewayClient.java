package com.lijunwei.agentplatformcenter.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lijunwei.agentplatformcenter.api.config.GatewayClientProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TenxAiGatewayClient {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final GatewayClientProperties properties;

    public TenxAiGatewayClient(RestTemplate restTemplate, ObjectMapper objectMapper, GatewayClientProperties properties) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public String chat(String configuredModel, String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (properties.getApiKey() != null && !properties.getApiKey().trim().isEmpty()) {
            headers.setBearerAuth(properties.getApiKey());
        }

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("model", normalizeModel(configuredModel));
        request.put("stream", false);
        request.put("temperature", 0.2);
        request.put("messages", messages(message));

        JsonNode response = restTemplate.postForObject(
                chatCompletionsUrl(),
                new HttpEntity<>(request, headers),
                JsonNode.class
        );
        return extractAssistantContent(response);
    }

    private String chatCompletionsUrl() {
        String baseUrl = properties.getBaseUrl();
        if (baseUrl.endsWith("/v1")) {
            return baseUrl + "/chat/completions";
        }
        if (baseUrl.endsWith("/")) {
            return baseUrl + "v1/chat/completions";
        }
        return baseUrl + "/v1/chat/completions";
    }

    private List<Map<String, String>> messages(String message) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(message("system", "You are an Agent Platform Center runtime model. Answer in Chinese unless the user asks otherwise."));
        messages.add(message("user", message));
        return messages;
    }

    private Map<String, String> message(String role, String content) {
        Map<String, String> item = new LinkedHashMap<>();
        item.put("role", role);
        item.put("content", content == null ? "" : content);
        return item;
    }

    String normalizeModel(String configuredModel) {
        if (configuredModel == null || configuredModel.trim().isEmpty()) {
            return "qwen3-coder-next";
        }
        String model = configuredModel.trim();
        int versionIndex = model.indexOf('@');
        if (versionIndex > -1) {
            model = model.substring(0, versionIndex);
        }
        if (model.startsWith("model-")) {
            model = model.substring("model-".length());
        }
        return model;
    }

    private String extractAssistantContent(JsonNode response) {
        JsonNode content = response == null ? null : response.at("/choices/0/message/content");
        if (content != null && content.isTextual()) {
            return content.asText();
        }
        return response == null ? "" : objectMapper.convertValue(response, JsonNode.class).toString();
    }
}
