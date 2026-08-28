package com.lijunwei.agentplatformcenter.api.service;

import com.lijunwei.agentplatformcenter.api.model.AgentDefinition;
import com.lijunwei.agentplatformcenter.api.model.CreateAgentRequest;
import com.lijunwei.agentplatformcenter.api.model.CreatePlatformResourceRequest;
import com.lijunwei.agentplatformcenter.api.model.PlatformResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AgentCatalogService {
    private final JdbcTemplate jdbcTemplate;

    public AgentCatalogService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AgentDefinition> listAgents() {
        return jdbcTemplate.query(
                "select id, name, description, model, prompt_version, skills, mcp_servers, tools, status from agent_definition order by created_at, id",
                agentMapper()
        );
    }

    public AgentDefinition createAgent(CreateAgentRequest request) {
        AgentDefinition agent = new AgentDefinition(
                "agent-" + UUID.randomUUID().toString().substring(0, 8),
                request.getName(),
                request.getDescription(),
                request.getModel(),
                request.getPromptVersion(),
                copyOrEmpty(request.getSkills()),
                copyOrEmpty(request.getMcpServers()),
                copyOrEmpty(request.getTools()),
                "draft"
        );
        jdbcTemplate.update(
                "insert into agent_definition (id, name, description, model, prompt_version, skills, mcp_servers, tools, status) values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                agent.getId(),
                agent.getName(),
                agent.getDescription(),
                agent.getModel(),
                agent.getPromptVersion(),
                join(agent.getSkills()),
                join(agent.getMcpServers()),
                join(agent.getTools()),
                agent.getStatus()
        );
        return agent;
    }

    public Optional<AgentDefinition> findAgent(String agentId) {
        List<AgentDefinition> agents = jdbcTemplate.query(
                "select id, name, description, model, prompt_version, skills, mcp_servers, tools, status from agent_definition where id = ?",
                agentMapper(),
                agentId
        );
        if (agents.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(agents.get(0));
    }

    private List<String> copyOrEmpty(List<String> values) {
        if (values == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(values);
    }

    private String join(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        return String.join(",", values);
    }

    public List<PlatformResource> listPrompts() {
        return listResourcesByType("prompt");
    }

    public List<PlatformResource> listSkills() {
        return listResourcesByType("skill");
    }

    public List<PlatformResource> listMcpServers() {
        return listResourcesByType("mcp");
    }

    public List<PlatformResource> listTools() {
        return listResourcesByType("tool");
    }

    public List<PlatformResource> listModels() {
        return listResourcesByType("model");
    }

    public PlatformResource createResource(String type, CreatePlatformResourceRequest request) {
        PlatformResource resource = new PlatformResource(
                resourceId(type, request.getName()),
                request.getName(),
                type,
                request.getVersion(),
                request.getDescription(),
                copyOrEmpty(request.getTags()),
                defaultString(request.getContent()),
                defaultString(request.getSchemaText()),
                "draft"
        );
        jdbcTemplate.update(
                "insert into platform_resource (id, name, type, version, description, tags, content, schema_text, status) values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                resource.getId(),
                resource.getName(),
                resource.getType(),
                resource.getVersion(),
                resource.getDescription(),
                join(resource.getTags()),
                resource.getContent(),
                resource.getSchemaText(),
                resource.getStatus()
        );
        return resource;
    }

    private String resourceId(String type, String name) {
        String normalized = name.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        if (normalized.isEmpty()) {
            normalized = UUID.randomUUID().toString().substring(0, 8);
        }
        return type + "-" + normalized + "-" + UUID.randomUUID().toString().substring(0, 6);
    }

    private List<PlatformResource> listResourcesByType(String type) {
        return jdbcTemplate.query(
                "select id, name, type, version, description, tags, content, schema_text, status from platform_resource where type = ? order by created_at, id",
                resourceMapper(),
                type
        );
    }

    private RowMapper<AgentDefinition> agentMapper() {
        return (rs, rowNum) -> new AgentDefinition(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("model"),
                rs.getString("prompt_version"),
                split(rs.getString("skills")),
                split(rs.getString("mcp_servers")),
                split(rs.getString("tools")),
                rs.getString("status")
        );
    }

    private RowMapper<PlatformResource> resourceMapper() {
        return (rs, rowNum) -> new PlatformResource(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("type"),
                rs.getString("version"),
                rs.getString("description"),
                split(rs.getString("tags")),
                rs.getString("content"),
                rs.getString("schema_text"),
                rs.getString("status")
        );
    }

    private List<String> split(String value) {
        if (value == null || value.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(value.split(","));
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
