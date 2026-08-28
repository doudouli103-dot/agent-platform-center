package com.lijunwei.agentplatformcenter.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lijunwei.agentplatformcenter.api.model.McpCallResult;
import com.lijunwei.agentplatformcenter.api.model.PlatformResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class RestApiMcpClientTests {

    @Test
    void executePostsConfiguredRestEndpointWithUserMessage() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        RestApiMcpClient client = new RestApiMcpClient(restTemplate, new ObjectMapper());
        PlatformResource mcp = new PlatformResource(
                "mcp-order-api",
                "Order API MCP",
                "mcp",
                "v1",
                "Call order project APIs",
                Collections.singletonList("order"),
                "",
                "{\"type\":\"rest-api\",\"baseUrl\":\"http://order-service.test\",\"endpoints\":[{\"name\":\"queryOrder\",\"method\":\"POST\",\"path\":\"/api/orders/search\",\"body\":{\"message\":\"${message}\"}}]}",
                "draft"
        );

        server.expect(once(), requestTo("http://order-service.test/api/orders/search"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(request -> assertBodyContains(request, "\"message\":\"order 1001\""))
                .andRespond(withSuccess("{\"orderNo\":\"1001\",\"status\":\"paid\"}", MediaType.APPLICATION_JSON));

        McpCallResult result = client.execute(mcp, "order 1001");

        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals("queryOrder", result.getEndpoint());
        Assertions.assertEquals(200, result.getStatusCode());
        Assertions.assertTrue(result.getResponseBody().contains("\"status\":\"paid\""));
        server.verify();
    }

    private static void assertBodyContains(ClientHttpRequest request, String expectedPart) throws IOException {
        String body = new String(((org.springframework.mock.http.client.MockClientHttpRequest) request).getBodyAsBytes(), StandardCharsets.UTF_8);
        Assertions.assertTrue(body.contains(expectedPart), body);
    }
}
