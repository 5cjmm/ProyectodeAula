package com.ShopMaster.API.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Red neuronal (perceptrón multicapa 222 → 64 → 32 → 1) entrenada en Python con el dataset de
 * Kaggle "Productos de Consumo Masivo" (notebook {@code ml/modelo_red_precios.ipynb}) y exportada a
 * {@code modelo_red_precios.json}.
 *
 * Estima el precio de un producto a partir de su subcategoría, marca, unidad de venta, cantidad y
 * unidad de medida. La red predice ln(precio); aquí se ejecuta el mismo cálculo que scikit-learn
 * (ReLU en las capas ocultas y salida lineal).
 */
@Component
public class ModeloRedPrecios {

    private static final String ARCHIVO = "modelo_red_precios.json";

    private final JsonNode raiz;
    private final Map<String, Integer> indice = new HashMap<>();
    private double mediaLogCant;
    private double stdLogCant;
    private double[][][] pesos;
    private double[][] sesgos;
    private final List<String> marcas = new ArrayList<>();

    public ModeloRedPrecios() {
        JsonNode leido = null;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(ARCHIVO)) {
            if (is != null) {
                leido = new ObjectMapper().readTree(is);
            }
        } catch (Exception e) {
            leido = null;
        }
        this.raiz = leido;
        if (raiz != null && raiz.has("red")) {
            cargar();
        }
    }

    private void cargar() {
        JsonNode columnas = raiz.get("entrada").get("columnas");
        for (int i = 0; i < columnas.size(); i++) {
            indice.put(columnas.get(i).asText(), i);
        }
        mediaLogCant = raiz.get("entrada").get("logCantidadMedia").asDouble();
        stdLogCant = raiz.get("entrada").get("logCantidadStd").asDouble();

        JsonNode w = raiz.get("red").get("pesos");
        JsonNode b = raiz.get("red").get("sesgos");
        pesos = new double[w.size()][][];
        sesgos = new double[b.size()][];
        for (int l = 0; l < w.size(); l++) {
            JsonNode capa = w.get(l);
            pesos[l] = new double[capa.size()][];
            for (int i = 0; i < capa.size(); i++) {
                JsonNode fila = capa.get(i);
                pesos[l][i] = new double[fila.size()];
                for (int j = 0; j < fila.size(); j++) {
                    pesos[l][i][j] = fila.get(j).asDouble();
                }
            }
            JsonNode sesgo = b.get(l);
            sesgos[l] = new double[sesgo.size()];
            for (int j = 0; j < sesgo.size(); j++) {
                sesgos[l][j] = sesgo.get(j).asDouble();
            }
        }
        for (JsonNode m : raiz.get("opciones").get("marcas")) {
            marcas.add(m.asText());
        }
    }

    public boolean disponible() {
        return pesos != null;
    }

    /**
     * Estima el precio. {@code cantidad} es opcional (null o ≤ 0 = sin cantidad).
     * Devuelve precioEstimado, minimo y maximo (rango típico, percentiles 10-90 de la validación).
     */
    public Map<String, Object> estimar(String subcategoria, String marca, String unidadVenta,
            Double cantidad, String unidadMedida) {
        if (!disponible()) {
            throw new IllegalStateException("El modelo de red neuronal no está disponible");
        }
        String sub = requerir(subcategoria, "la subcategoría");
        String venta = requerir(unidadVenta, "la unidad de venta");
        String medida = unidadMedida == null || unidadMedida.isBlank() ? "ninguna" : unidadMedida.trim();
        String marcaFinal = marca != null && marcas.contains(marca.trim()) ? marca.trim() : "OTRA";

        double[] x = new double[indice.size()];
        marcar(x, "sub:" + sub, "La subcategoría no es válida: " + sub);
        marcar(x, "marca:" + marcaFinal, "La marca no es válida: " + marcaFinal);
        marcar(x, "venta:" + venta, "La unidad de venta no es válida: " + venta);
        marcar(x, "med:" + medida, "La unidad de medida no es válida: " + medida);

        boolean sinCantidad = cantidad == null || cantidad <= 0 || cantidad.isNaN() || "ninguna".equals(medida);
        double logCant = sinCantidad ? 0.0 : Math.log1p(cantidad);
        x[indice.get("log_cantidad")] = (logCant - mediaLogCant) / stdLogCant;
        x[indice.get("sin_cantidad")] = sinCantidad ? 1.0 : 0.0;

        double precio = Math.exp(propagar(x));
        JsonNode m = raiz.get("metricas");
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("precioEstimado", redondear(precio));
        r.put("minimo", redondear(precio * m.get("factorMinimo").asDouble()));
        r.put("maximo", redondear(precio * m.get("factorMaximo").asDouble()));
        return r;
    }

    /** Propagación hacia adelante: ReLU en las capas ocultas y salida lineal (ln precio). */
    private double propagar(double[] entrada) {
        double[] a = entrada;
        for (int l = 0; l < pesos.length; l++) {
            double[] siguiente = new double[sesgos[l].length];
            for (int j = 0; j < siguiente.length; j++) {
                double s = sesgos[l][j];
                for (int i = 0; i < a.length; i++) {
                    if (a[i] != 0.0) {
                        s += a[i] * pesos[l][i][j];
                    }
                }
                siguiente[j] = (l < pesos.length - 1) ? Math.max(0.0, s) : s;
            }
            a = siguiente;
        }
        return a[0];
    }

    private void marcar(double[] x, String columna, String mensajeError) {
        Integer i = indice.get(columna);
        if (i == null) {
            throw new IllegalArgumentException(mensajeError);
        }
        x[i] = 1.0;
    }

    private static String requerir(String valor, String nombre) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Indica " + nombre);
        }
        return valor.trim();
    }

    private static double redondear(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    /** Listas para los selectores del formulario (subcategorías, marcas, unidades). */
    public Map<String, Object> opciones() {
        Map<String, Object> m = new LinkedHashMap<>();
        if (disponible()) {
            m.put("opciones", new ObjectMapper().convertValue(raiz.get("opciones"), Object.class));
        }
        return m;
    }

    /** Ficha del modelo (variable predictiva, predictoras, métricas y comparativa), sin los pesos. */
    public Map<String, Object> ficha() {
        Map<String, Object> m = new LinkedHashMap<>();
        if (!disponible()) {
            return m;
        }
        ObjectMapper om = new ObjectMapper();
        for (String campo : new String[] { "version", "tipoProblema", "modelo", "arquitectura", "variablePredictiva",
                "variablesPredictoras", "dataset", "observaciones", "validacion", "metricas", "comparativa" }) {
            if (raiz.has(campo)) {
                m.put(campo, om.convertValue(raiz.get(campo), Object.class));
            }
        }
        return m;
    }

    /** Casos de verificación exportados desde Python (para pruebas). */
    public JsonNode casosVerificacion() {
        return raiz == null ? null : raiz.get("verificacion");
    }
}
