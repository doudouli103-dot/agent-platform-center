package com.lijunwei.agentplatformcenter.api.model;

public class RunRecord {
    private String id;
    private String agentId;
    private String agentName;
    private String model;
    private String userMessage;
    private String assistantOutput;
    private String status;
    private String gatewayData;
    private String traceData;
    private String createdAt;
    private String completedAt;

    public RunRecord() {
    }

    public RunRecord(String id, String agentId, String agentName, String model, String userMessage,
                     String assistantOutput, String status, String gatewayData, String traceData,
                     String createdAt, String completedAt) {
        this.id = id;
        this.agentId = agentId;
        this.agentName = agentName;
        this.model = model;
        this.userMessage = userMessage;
        this.assistantOutput = assistantOutput;
        this.status = status;
        this.gatewayData = gatewayData;
        this.traceData = traceData;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getAssistantOutput() {
        return assistantOutput;
    }

    public void setAssistantOutput(String assistantOutput) {
        this.assistantOutput = assistantOutput;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGatewayData() {
        return gatewayData;
    }

    public void setGatewayData(String gatewayData) {
        this.gatewayData = gatewayData;
    }

    public String getTraceData() {
        return traceData;
    }

    public void setTraceData(String traceData) {
        this.traceData = traceData;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(String completedAt) {
        this.completedAt = completedAt;
    }
}
