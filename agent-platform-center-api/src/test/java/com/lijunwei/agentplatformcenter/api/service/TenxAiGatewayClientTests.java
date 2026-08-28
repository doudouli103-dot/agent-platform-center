package com.lijunwei.agentplatformcenter.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lijunwei.agentplatformcenter.api.config.GatewayClientProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TenxAiGatewayClientTests {

    @Test
    void chatUsesOpenAiCompatibleGatewayAndExtractsAssistantContent() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        GatewayClientProperties properties = new GatewayClientProperties();
        properties.setBaseUrl("http://127.0.0.1:8088");
        properties.setApiKey("local-dev-key");
        TenxAiGatewayClient client = new TenxAiGatewayClient(restTemplate, new ObjectMapper(), properties);

        server.expect(once(), requestTo("http://127.0.0.1:8088/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer local-dev-key"))
                .andExpect(request -> assertBodyContains(request, "\"model\":\"qwen3-coder-next\"", "\"content\":\"hello\""))
                .andRespond(withSuccess("{\"choices\":[{\"message\":{\"content\":\"gateway reply\"}}]}", MediaType.APPLICATION_JSON));

        String content = client.chat("model-qwen3-coder-next@v1", "hello");

        Assertions.assertEquals("gateway reply", content);
        server.verify();
    }

    private static void assertBodyContains(ClientHttpRequest request, String... expectedParts) throws IOException {
        String body = request.getBody().toString();
        if (body.startsWith("org.springframework")) {
            body = new String(((org.springframework.mock.http.client.MockClientHttpRequest) request).getBodyAsBytes(), StandardCharsets.UTF_8);
        }
        for (String expectedPart : expectedParts) {
            Assertions.assertTrue(body.contains(expectedPart), body);
        }
    }
}
