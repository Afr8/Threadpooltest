package com.example.requestor.model;

import java.util.Set;

public class SummaryRequest {

    private String text;
    private Set<String> models;

    public SummaryRequest() {
    }

    public SummaryRequest(String text, Set<String> models) {
        this.text = text;
        this.models = models;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Set<String> getModels() {
        return models;
    }

    public void setModels(Set<String> models) {
        this.models = models;
    }
}
