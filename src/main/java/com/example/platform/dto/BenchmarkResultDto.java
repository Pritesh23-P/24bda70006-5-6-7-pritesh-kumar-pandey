package com.example.platform.dto;

public class BenchmarkResultDto {

    private String strategy;
    private int recordCount;
    private long executionTimeMs;
    private int queryCountEstimated;
    private String description;

    public BenchmarkResultDto() {
    }

    public BenchmarkResultDto(String strategy, int recordCount, long executionTimeMs, int queryCountEstimated, String description) {
        this.strategy = strategy;
        this.recordCount = recordCount;
        this.executionTimeMs = executionTimeMs;
        this.queryCountEstimated = queryCountEstimated;
        this.description = description;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public int getQueryCountEstimated() {
        return queryCountEstimated;
    }

    public void setQueryCountEstimated(int queryCountEstimated) {
        this.queryCountEstimated = queryCountEstimated;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
