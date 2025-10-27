package com.ShopMaster.API.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class GeminiService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);

    // Use empty default so missing property doesn't prevent bean creation
    @Value("${gemini.api.key:}")
    private String apiKey;

    private final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    public String getSuggestion(String predictionText, String patientSummary) {
        try {
            if (apiKey == null || apiKey.isBlank()) {
                logger.warn("La propiedad 'gemini.api.key' no está configurada. Se usará un fallback local en lugar de llamar a la API.");
                // Fallback simple y seguro para entornos de desarrollo
                return "(Fallback) Consulte con un cardiólogo: siga las guías clínicas locales y realice más pruebas diagnósticas según corresponda.";
            }
            String prompt = "Soy un cardiólogo. Basado en el siguiente caso clínico:\n"
                    + "Resultado de predicción: " + predictionText + ".\n"
                    + "Resumen del paciente: " + patientSummary + ".\n"
                    + "Dame una recomendación clínica breve (máximo 2 líneas), clara y directa para este caso.";


            String requestBody = """
                    {
                    "contents": [
                        {
                        "parts": [
                            {
                            "text": "%s"
                            }
                        ]
                        }
                    ]
                    }
                    """.formatted(prompt);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "?key=" + apiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            String responseBody = response.body();
            // System.out.println("Respuesta completa del API Gemini:");
            // System.out.println(responseBody);

            // Parsear usando org.json
            JSONObject json = new JSONObject(responseBody);
            JSONArray candidates = json.getJSONArray("candidates");
            JSONObject content = candidates.getJSONObject(0).getJSONObject("content");
            JSONArray parts = content.getJSONArray("parts");
            String suggestion = parts.getJSONObject(0).getString("text");

            return suggestion;

        } catch (Exception e) {
            e.printStackTrace();
            return "No se pudo obtener una sugerencia del modelo Gemini.";
        }
    }

}

