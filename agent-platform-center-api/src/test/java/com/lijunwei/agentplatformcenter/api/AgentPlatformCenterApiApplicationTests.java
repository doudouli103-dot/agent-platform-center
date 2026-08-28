package com.lijunwei.agentplatformcenter.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AgentPlatformCenterApiApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void healthReturnsUp() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void listAgentsReturnsSeedAgents() throws Exception {
        mockMvc.perform(get("/api/agents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[0].id").value("agent-java-architect"));
    }

    @Test
    void createAgentStoresBindingsAndReturnsDraft() throws Exception {
        mockMvc.perform(post("/api/agents")
                        .contentType("application/json")
                        .content("{\"name\":\"Code Reviewer\",\"description\":\"Review Java code\",\"model\":\"qwen3-coder-next\",\"promptVersion\":\"prompt-java-architect@v1\",\"skills\":[\"skill-java-review@v1\"],\"mcpServers\":[\"mcp-filesystem@v1\"],\"tools\":[\"tool-shell@v1\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Code Reviewer"))
                .andExpect(jsonPath("$.status").value("draft"))
                .andExpect(jsonPath("$.skills[0]").value("skill-java-review@v1"))
                .andExpect(jsonPath("$.mcpServers[0]").value("mcp-filesystem@v1"))
                .andExpect(jsonPath("$.tools[0]").value("tool-shell@v1"));

        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from agent_definition where name = ?",
                Integer.class,
                "Code Reviewer"
        );
        org.junit.jupiter.api.Assertions.assertEquals(1, count);
    }

    @Test
    void createRunReturnsEventUrl() throws Exception {
        mockMvc.perform(post("/api/runs")
                        .contentType("application/json")
                        .content("{\"agentId\":\"agent-java-architect\",\"message\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("created"))
                .andExpect(jsonPath("$.eventsUrl").exists());
    }

    @Test
    void listPromptsReturnsSeedPrompts() throws Exception {
        mockMvc.perform(get("/api/prompts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[*].id", hasItem("prompt-java-architect")));
    }

    @Test
    void listSkillsReturnsSeedSkills() throws Exception {
        mockMvc.perform(get("/api/skills"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[*].id", hasItem("skill-java-review")));
    }

    @Test
    void listMcpServersReturnsSeedServers() throws Exception {
        mockMvc.perform(get("/api/mcp-servers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[*].id", hasItem("mcp-filesystem")));
    }

    @Test
    void listToolsReturnsSeedTools() throws Exception {
        mockMvc.perform(get("/api/tools"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[*].id", hasItem("tool-shell")));
    }

    @Test
    void listModelsReturnsSeedModels() throws Exception {
        mockMvc.perform(get("/api/models"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[*].id", hasItem("model-qwen3-coder-next")))
                .andExpect(jsonPath("$[0].schemaText").value(org.hamcrest.Matchers.containsString("\"capabilities\"")));
    }

    @Test
    void createPromptStoresResource() throws Exception {
        mockMvc.perform(post("/api/prompts")
                        .contentType("application/json")
                        .content("{\"name\":\"Business Analyst Prompt\",\"version\":\"v1\",\"description\":\"Analyze business requirements\",\"tags\":[\"business\",\"analysis\"],\"content\":\"You are a business analyst.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.type").value("prompt"))
                .andExpect(jsonPath("$.name").value("Business Analyst Prompt"))
                .andExpect(jsonPath("$.tags[0]").value("business"))
                .andExpect(jsonPath("$.content").value("You are a business analyst."));
    }

    @Test
    void createSkillStoresResource() throws Exception {
        mockMvc.perform(post("/api/skills")
                        .contentType("application/json")
                        .content("{\"name\":\"SQL Review\",\"version\":\"v1\",\"description\":\"Review SQL lineage and predicates\",\"tags\":[\"sql\",\"review\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("skill"))
                .andExpect(jsonPath("$.name").value("SQL Review"));
    }

    @Test
    void createMcpServerStoresResource() throws Exception {
        mockMvc.perform(post("/api/mcp-servers")
                        .contentType("application/json")
                        .content("{\"name\":\"GitLab MCP\",\"version\":\"v1\",\"description\":\"Read GitLab repositories\",\"tags\":[\"gitlab\",\"repo\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("mcp"))
                .andExpect(jsonPath("$.name").value("GitLab MCP"));
    }

    @Test
    void createToolStoresResource() throws Exception {
        mockMvc.perform(post("/api/tools")
                        .contentType("application/json")
                        .content("{\"name\":\"HTTP Request\",\"version\":\"v1\",\"description\":\"Call approved HTTP endpoints\",\"tags\":[\"http\",\"integration\"],\"schemaText\":\"{\\\"url\\\":\\\"string\\\"}\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("tool"))
                .andExpect(jsonPath("$.name").value("HTTP Request"))
                .andExpect(jsonPath("$.schemaText").value("{\"url\":\"string\"}"));
    }

    @Test
    void createModelStoresResource() throws Exception {
        mockMvc.perform(post("/api/models")
                        .contentType("application/json")
                        .content("{\"name\":\"Local Qwen\",\"version\":\"v1\",\"description\":\"Local OpenAI-compatible Qwen model\",\"tags\":[\"local\",\"coder\"],\"schemaText\":\"{\\\"provider\\\":\\\"openai-compatible\\\",\\\"baseUrl\\\":\\\"http://localhost:4000/v1\\\"}\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("model"))
                .andExpect(jsonPath("$.name").value("Local Qwen"))
                .andExpect(jsonPath("$.schemaText").value("{\"provider\":\"openai-compatible\",\"baseUrl\":\"http://localhost:4000/v1\"}"));
    }
}
