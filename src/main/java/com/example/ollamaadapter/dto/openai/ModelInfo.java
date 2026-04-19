package com.example.ollamaadapter.dto.openai;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ModelInfo {

    private String id;
    private String object;
    private long created;
    private String ownedBy;

    public ModelInfo() {
    }

    public ModelInfo(String id, String object, long created, String ownedBy) {
        this.id = id;
        this.object = object;
        this.created = created;
        this.ownedBy = ownedBy;
    }

    @JsonProperty
    public String getId() {
        return id;
    }

    @JsonProperty
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty
    public String getObject() {
        return object;
    }

    @JsonProperty
    public void setObject(String object) {
        this.object = object;
    }

    @JsonProperty
    public long getCreated() {
        return created;
    }

    @JsonProperty
    public void setCreated(long created) {
        this.created = created;
    }

    @JsonProperty("owned_by")
    public String getOwnedBy() {
        return ownedBy;
    }

    @JsonProperty("owned_by")
    public void setOwnedBy(String ownedBy) {
        this.ownedBy = ownedBy;
    }
}
