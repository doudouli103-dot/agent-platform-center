package com.lijunwei.agentplatformcenter.api.model;

import javax.validation.constraints.NotBlank;
import java.util.List;

public class CreateAgentRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotBlank
    private String model;

    @NotBlank
    private String promptVersion;

    private List<String> skills;
    private List<String> mcpServers;
    private List<String> tools;

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
}
