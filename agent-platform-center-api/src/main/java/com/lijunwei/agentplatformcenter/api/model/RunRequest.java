package com.lijunwei.agentplatformcenter.api.model;

import javax.validation.constraints.NotBlank;

public class RunRequest {
    @NotBlank
    private String agentId;

    @NotBlank
    private String message;

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
