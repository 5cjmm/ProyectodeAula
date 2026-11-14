package com.ShopMaster.API.controller;

import com.ShopMaster.API.dto.EstimacionRequest;
import com.ShopMaster.API.dto.PredictionResponse;
import com.ShopMaster.API.dto.PriceRequest;
import com.ShopMaster.API.dto.PriceResponse;
import com.ShopMaster.API.model.PatientData;
import com.ShopMaster.API.service.ModeloRedPrecios;
import com.ShopMaster.API.service.PredictionService;
import com.ShopMaster.API.service.PricePredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/predict")
public class PredictionController {

    @Autowired
    private PredictionService predictionService;

    @Autowired
    private PricePredictionService pricePredictionService;

    @Autowired
    private ModeloRedPrecios redPrecios;

    @PostMapping
    public PredictionResponse predict(@RequestBody PatientData data) {
        String prediction = predictionService.predict(data);

        String suggestion;
        try {
            suggestion = predictionService.getGeminiSuggestion(prediction, data);
        } catch (Exception e) {
            suggestion = "No se pudo obtener una sugerencia del modelo Gemini.";
        }

        List<String> stats = predictionService.generateStatistics(data);

        return new PredictionResponse(prediction, suggestion, stats);
    }

    @PostMapping("/price")
    public PriceResponse predictPrice(@RequestBody PriceRequest request) {
        // If startYear is null, service will default to current year
        Map<Integer, Double> preds = pricePredictionService.predictMultiYear(request.getProduct(), request.getStartYear(), request.getYears());
        Map<Integer, double[]> intervals = new java.util.LinkedHashMap<>();
        for (Integer year : preds.keySet()) {
            double[] iv = pricePredictionService.predictInterval(request.getProduct(), year);
            if (iv != null) {
                intervals.put(year, iv);
            }
        }
        return new PriceResponse(preds, intervals.isEmpty() ? null : intervals, pricePredictionService.getModelInfo());
    }

    /** Ficha del modelo: variable predictiva, predictoras, porcentaje predictivo y tabla comparativa. */
    @GetMapping("/price/model")
    public Map<String, Object> modelInfo() {
        return pricePredictionService.getModelInfo();
    }

    /** Opciones del formulario y ficha de la red neuronal (variable predictiva, métricas, comparativa). */
    @GetMapping("/estimar/opciones")
    public Map<String, Object> estimarOpciones() {
        Map<String, Object> r = new java.util.LinkedHashMap<>(redPrecios.opciones());
        r.put("modelo", redPrecios.ficha());
        return r;
    }

    /** Estima el precio de un producto con la red neuronal entrenada con el dataset de Kaggle. */
    @PostMapping("/estimar")
    public Map<String, Object> estimarPrecio(@RequestBody EstimacionRequest req) {
        if (!redPrecios.disponible()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, "El modelo de red neuronal no está disponible");
        }
        try {
            Map<String, Object> r = redPrecios.estimar(req.getSubcategoria(), req.getMarca(), req.getUnidadVenta(),
                    req.getCantidad(), req.getUnidadMedida());
            r.put("modelo", redPrecios.ficha());
            return r;
        } catch (IllegalArgumentException e) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/price/products")
    public List<String> listProducts() {
        return pricePredictionService.getProductCategories();
    }

    @GetMapping("/price/history")
    public Map<Integer, Double> getHistory(@RequestParam String product) {
        return pricePredictionService.getHistory(product);
    }
}

