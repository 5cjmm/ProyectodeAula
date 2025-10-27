package com.ShopMaster.API.dto;

import java.util.List;

public class PredictionResponse {
    private String prediction;
    private String suggestion;
    private List<String> statistics;

    public PredictionResponse(String prediction, String suggestion, List<String> statistics) {
        this.prediction = prediction;
        this.suggestion = suggestion;
        this.statistics = statistics;
    }

    public String getPrediction() {
        return prediction;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public List<String> getStatistics() {
        return statistics;
    }
}
