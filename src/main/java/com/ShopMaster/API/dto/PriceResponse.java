package com.ShopMaster.API.dto;

import java.util.Map;

public class PriceResponse {
    // Map year -> predicted price
    private Map<Integer, Double> predictions;
    // Map year -> [mínimo, máximo] del intervalo de confianza del 95 % (puede ser null)
    private Map<Integer, double[]> intervals;
    // Ficha del modelo: variable predictiva, predictoras, porcentaje predictivo, comparativa...
    private Map<String, Object> model;

    public PriceResponse() {
    }

    public PriceResponse(Map<Integer, Double> predictions) {
        this.predictions = predictions;
    }

    public PriceResponse(Map<Integer, Double> predictions, Map<Integer, double[]> intervals, Map<String, Object> model) {
        this.predictions = predictions;
        this.intervals = intervals;
        this.model = model;
    }

    public Map<Integer, double[]> getIntervals() {
        return intervals;
    }

    public void setIntervals(Map<Integer, double[]> intervals) {
        this.intervals = intervals;
    }

    public Map<String, Object> getModel() {
        return model;
    }

    public void setModel(Map<String, Object> model) {
        this.model = model;
    }

    public Map<Integer, Double> getPredictions() {
        return predictions;
    }

    public void setPredictions(Map<Integer, Double> predictions) {
        this.predictions = predictions;
    }
}
