package com.lijunwei.agentplatformcenter.api.model;

import java.util.List;

public class AgentDefinition {
    private String id;
    private String name;
    private String description;
    private String model;
    private String promptVersion;
    private List<String> skills;
    private List<String> mcpServers;
    private List<String> tools;
    private String status;

    public AgentDefinition() {
    }

    public AgentDefinition(String id, String name, String description, String model, String promptVersion,
                           List<String> skills, List<String> mcpServers, List<String> tools, String status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.model = model;
        this.promptVersion = promptVersion;
        this.skills = skills;
        this.mcpServers = mcpServers;
        this.tools = tools;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPromptVersion() {
        return promptVersion;
    }

    public void setPromptVersion(String promptVersion) {
        this.promptVersion = promptVersion;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public List<String> getMcpServers() {
        return mcpServers;
    }

    public void setMcpServers(List<String> mcpServers) {
        this.mcpServers = mcpServers;
    }

    public List<String> getTools() {
        return tools;
    }

    public void setTools(List<String> tools) {
        this.tools = tools;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
