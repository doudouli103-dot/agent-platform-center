package com.lijunwei.agentplatformcenter.api.model;

import java.util.List;

public class PlatformResource {
    private String id;
    private String name;
    private String type;
    private String version;
    private String description;
    private List<String> tags;
    private String content;
    private String schemaText;
    private String status;

    public PlatformResource() {
    }

    public PlatformResource(String id, String name, String type, String version, String description,
                            List<String> tags, String content, String schemaText, String status) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.version = version;
        this.description = description;
        this.tags = tags;
        this.content = content;
        this.schemaText = schemaText;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSchemaText() {
        return schemaText;
    }

    public void setSchemaText(String schemaText) {
        this.schemaText = schemaText;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
