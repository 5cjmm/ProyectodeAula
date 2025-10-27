package com.ShopMaster.API.dto;

import java.util.Map;

public class PriceResponse {
    // Map year -> predicted price
    private Map<Integer, Double> predictions;

    public PriceResponse() {
    }

    public PriceResponse(Map<Integer, Double> predictions) {
        this.predictions = predictions;
    }

    public Map<Integer, Double> getPredictions() {
        return predictions;
    }

    public void setPredictions(Map<Integer, Double> predictions) {
        this.predictions = predictions;
    }
}
