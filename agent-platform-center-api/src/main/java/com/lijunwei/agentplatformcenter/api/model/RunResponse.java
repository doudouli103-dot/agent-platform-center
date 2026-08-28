package com.lijunwei.agentplatformcenter.api.model;

public class RunResponse {
    private String runId;
    private String status;
    private String eventsUrl;

    public RunResponse() {
    }

    public RunResponse(String runId, String status, String eventsUrl) {
        this.runId = runId;
        this.status = status;
        this.eventsUrl = eventsUrl;
    }

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEventsUrl() {
        return eventsUrl;
    }

    public void setEventsUrl(String eventsUrl) {
        this.eventsUrl = eventsUrl;
    }
}
