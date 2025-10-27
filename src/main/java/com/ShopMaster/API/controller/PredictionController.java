package com.ShopMaster.API.controller;

import com.ShopMaster.API.dto.PredictionResponse;
import com.ShopMaster.API.dto.PriceRequest;
import com.ShopMaster.API.dto.PriceResponse;
import com.ShopMaster.API.model.PatientData;
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
        return new PriceResponse(preds);
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

