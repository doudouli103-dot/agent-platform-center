package com.lijunwei.agentplatformcenter.api.model;

public class McpCallResult {
    private String mcpId;
    private String endpoint;
    private String method;
    private String url;
    private int statusCode;
    private String responseBody;
    private boolean success;
    private String errorMessage;

    public McpCallResult() {
    }

    public McpCallResult(String mcpId, String endpoint, String method, String url, int statusCode,
                         String responseBody, boolean success, String errorMessage) {
        this.mcpId = mcpId;
        this.endpoint = endpoint;
        this.method = method;
        this.url = url;
        this.statusCode = statusCode;
        this.responseBody = responseBody;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public static McpCallResult skipped(String mcpId, String reason) {
        return new McpCallResult(mcpId, "", "", "", 0, "", false, reason);
    }

    public String getMcpId() {
        return mcpId;
    }

    public void setMcpId(String mcpId) {
        this.mcpId = mcpId;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
