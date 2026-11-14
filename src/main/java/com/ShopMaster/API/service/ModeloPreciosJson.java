package com.ShopMaster.API.service;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Modelo de predicción de precios entrenado en Python (notebook {@code ml/modelo_precios.ipynb})
 * y exportado a {@code modelo_precios.json}.
 *
 * Es una regresión log-lineal por producto: {@code precio = exp(a + b · año)}. El archivo
 * también trae las métricas de validación (porcentaje predictivo, R², MAPE...) y la tabla
 * comparativa de los modelos evaluados. Si el archivo no existe, {@link #disponible()} es falso
 * y el servicio sigue usando el modelo de Weka.
 */
@Component
public class ModeloPreciosJson {

    private static final String ARCHIVO = "modelo_precios.json";
    /** Valor z del intervalo de confianza del 95 %. */
    private static final double Z95 = 1.96;

    private final JsonNode raiz;

    public ModeloPreciosJson() {
        JsonNode leido = null;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(ARCHIVO)) {
            if (is != null) {
                leido = new ObjectMapper().readTree(is);
            }
        } catch (Exception e) {
            leido = null; // se usará el modelo de respaldo
        }
        this.raiz = leido;
    }

    public boolean disponible() {
        return raiz != null && raiz.has("productos");
    }

    public boolean contiene(String producto) {
        return disponible() && producto != null && raiz.get("productos").has(producto);
    }

    /** Precio pronosticado para el año indicado. */
    public double predecir(String producto, int anio) {
        JsonNode p = raiz.get("productos").get(producto);
        return Math.exp(p.get("a").asDouble() + p.get("b").asDouble() * anio);
    }

    /** Intervalo de confianza del 95 %: {mínimo, máximo}. */
    public double[] intervalo(String producto, int anio) {
        JsonNode p = raiz.get("productos").get(producto);
        double mu = p.get("a").asDouble() + p.get("b").asDouble() * anio;
        double z = Z95 * sigmaLog();
        return new double[] { Math.exp(mu - z), Math.exp(mu + z) };
    }

    public double sigmaLog() {
        return raiz.path("metricas").path("sigmaLog").asDouble(0);
    }

    /** Porcentaje predictivo del modelo (100 − MAPE del hold-out temporal). */
    public double porcentajePredictivo() {
        return raiz.path("metricas").path("porcentajePredictivo").asDouble(Double.NaN);
    }

    /** Crecimiento anual compuesto que el modelo estima para el producto, en %. */
    public Double crecimientoAnualPct(String producto) {
        if (!contiene(producto)) {
            return null;
        }
        return raiz.get("productos").get(producto).path("crecimientoAnualPct").asDouble();
    }

    /** Ficha del modelo para mostrar en la web y en la app (sin los parámetros por producto). */
    public Map<String, Object> ficha() {
        Map<String, Object> m = new LinkedHashMap<>();
        if (!disponible()) {
            return m;
        }
        ObjectMapper om = new ObjectMapper();
        for (String campo : new String[] { "version", "tipoProblema", "modelo", "formula", "variablePredictiva",
                "variablesPredictoras", "dataset", "observaciones", "validacion", "metricas", "comparativa" }) {
            if (raiz.has(campo)) {
                m.put(campo, om.convertValue(raiz.get(campo), Object.class));
            }
        }
        return m;
    }
}
