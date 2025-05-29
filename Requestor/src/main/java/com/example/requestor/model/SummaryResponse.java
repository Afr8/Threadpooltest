package com.example.requestor.model;

import java.util.Map;

public class SummaryResponse {
    private Map<String, String> summaries;

    // No-args constructor
    public SummaryResponse() {
    }

    // All-args constructor
    public SummaryResponse(Map<String, String> summaries) {
        this.summaries = summaries;
    }

    // Getter
    public Map<String, String> getSummaries() {
        return summaries;
    }

    // Setter
    public void setSummaries(Map<String, String> summaries) {
        this.summaries = summaries;
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Map<String, String> summaries;

        public Builder summaries(Map<String, String> summaries) {
            this.summaries = summaries;
            return this;
        }

        public SummaryResponse build() {
            return new SummaryResponse(summaries);
        }
    }
}
