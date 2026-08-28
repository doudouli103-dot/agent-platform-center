package com.lijunwei.agentplatformcenter.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lijunwei.agentplatformcenter.api.model.McpCallResult;
import com.lijunwei.agentplatformcenter.api.model.PlatformResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

@Service
public class RestApiMcpClient {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public RestApiMcpClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public McpCallResult execute(PlatformResource mcp, String message) {
        try {
            JsonNode schema = parseSchema(mcp);
            if (!"rest-api".equals(schema.path("type").asText())) {
                return McpCallResult.skipped(mcp.getId(), "MCP schema type is not rest-api.");
            }
            JsonNode endpoint = firstEndpoint(schema);
            if (endpoint == null) {
                return McpCallResult.skipped(mcp.getId(), "MCP schema has no endpoint.");
            }

            String method = endpoint.path("method").asText("GET").toUpperCase();
            String url = joinUrl(schema.path("baseUrl").asText(), endpoint.path("path").asText().replace("${message}", defaultString(message)));
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);
            applyAuth(headers, schema.path("auth"));

            Object body = null;
            if (endpoint.has("body")) {
                body = replaceMessage(endpoint.get("body"), message);
            }
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.resolve(method) == null ? HttpMethod.GET : HttpMethod.resolve(method),
                    new HttpEntity<>(body, headers),
                    String.class
            );

            return new McpCallResult(
                    mcp.getId(),
                    endpoint.path("name").asText("default"),
                    method,
                    url,
                    response.getStatusCodeValue(),
                    defaultString(response.getBody()),
                    response.getStatusCode().is2xxSuccessful(),
                    ""
            );
        } catch (Exception ex) {
            return new McpCallResult(mcp.getId(), "", "", "", 0, "", false, ex.getMessage());
        }
    }

    private JsonNode parseSchema(PlatformResource mcp) throws java.io.IOException {
        if (mcp.getSchemaText() == null || mcp.getSchemaText().trim().isEmpty()) {
            return objectMapper.createObjectNode();
        }
        return objectMapper.readTree(mcp.getSchemaText());
    }

    private JsonNode firstEndpoint(JsonNode schema) {
        JsonNode endpoints = schema.path("endpoints");
        if (!endpoints.isArray() || endpoints.size() == 0) {
            return null;
        }
        return endpoints.get(0);
    }

    private Object replaceMessage(JsonNode node, String message) {
        if (node.isTextual()) {
            return node.asText().replace("${message}", defaultString(message));
        }
        if (node.isObject()) {
            ObjectNode copy = objectMapper.createObjectNode();
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                copy.set(field.getKey(), objectMapper.valueToTree(replaceMessage(field.getValue(), message)));
            }
            return copy;
        }
        if (node.isArray()) {
            ArrayNode copy = objectMapper.createArrayNode();
            for (JsonNode item : node) {
                copy.add(objectMapper.valueToTree(replaceMessage(item, message)));
            }
            return copy;
        }
        return node;
    }

    private void applyAuth(HttpHeaders headers, JsonNode auth) {
        String type = auth.path("type").asText("");
        String envName = auth.path("env").asText("");
        String value = envName.isEmpty() ? "" : System.getenv(envName);
        if (value == null || value.isEmpty()) {
            return;
        }
        if ("bearer".equalsIgnoreCase(type)) {
            headers.setBearerAuth(value);
        }
        if ("api-key".equalsIgnoreCase(type)) {
            headers.set("X-API-Key", value);
        }
    }

    private String joinUrl(String baseUrl, String path) {
        if (baseUrl.endsWith("/") && path.startsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1) + path;
        }
        if (!baseUrl.endsWith("/") && !path.startsWith("/")) {
            return baseUrl + "/" + path;
        }
        return baseUrl + path;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
