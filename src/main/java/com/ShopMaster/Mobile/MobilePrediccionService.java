package com.ShopMaster.Mobile;

import java.text.Collator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import com.ShopMaster.API.service.PricePredictionService;

/**
 * Predicción de precios para la app móvil: toma el historial y el modelo de Weka que ya
 * existen en la web (PricePredictionService) y compara el pronóstico con la inflación
 * oficial del DANE (nacional y Cartagena).
 *
 * El modelo se entrenó con precios de 2015 a 2024 del dataset de consumo masivo; el
 * pronóstico arranca el año siguiente al último dato histórico.
 */
@Service
public class MobilePrediccionService {

    static final int ANIOS_POR_DEFECTO = 3;
    static final int ANIOS_MAXIMO = 5;

    /** Diferencia (en puntos porcentuales) a partir de la cual se considera que "sube más" o "sube menos". */
    static final double UMBRAL_PUNTOS = 1.0;

    static final String POR_ENCIMA = "POR_ENCIMA";
    static final String EN_LINEA = "EN_LINEA";
    static final String POR_DEBAJO = "POR_DEBAJO";

    private static final Locale ES_CO = Locale.forLanguageTag("es-CO");

    private final PricePredictionService precios;

    public MobilePrediccionService(PricePredictionService precios) {
        this.precios = precios;
    }

    // ── GET /api/prediccion/productos ─────────────────────────────────────────

    public Map<String, Object> productos() {
        List<Map<String, Object>> lista = new ArrayList<>();
        for (String nombre : precios.getProductCategories()) {
            TreeMap<Integer, Double> historial = new TreeMap<>(precios.getHistory(nombre));
            if (historial.isEmpty()) {
                continue; // p. ej. el marcador "unknown" cuando no hay dataset
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("nombre", nombre);
            item.put("ultimoAnio", historial.lastKey());
            item.put("ultimoPrecio", redondear(historial.lastEntry().getValue()));
            lista.add(item);
        }
        Collator collator = Collator.getInstance(ES_CO);
        lista.sort((a, b) -> collator.compare((String) a.get("nombre"), (String) b.get("nombre")));
        return MobileApiResponse.ok(lista);
    }

    // ── GET /api/prediccion/precios?producto=...&anios=3 ──────────────────────

    public Map<String, Object> precios(String producto, String aniosTexto) {
        String buscado = producto == null ? "" : producto.trim();
        if (buscado.isEmpty()) {
            throw MobileException.badRequest("Indica el producto que quieres consultar");
        }
        int anios = parsearAnios(aniosTexto);

        String nombre = null;
        for (String candidato : precios.getProductCategories()) {
            if (candidato.equalsIgnoreCase(buscado)) {
                nombre = candidato;
                break;
            }
        }
        if (nombre == null) {
            throw MobileException.notFound("Producto no encontrado en el modelo de precios");
        }

        TreeMap<Integer, Double> historial = new TreeMap<>(precios.getHistory(nombre));
        if (historial.isEmpty()) {
            throw MobileException.notFound("No hay historial de precios para este producto");
        }
        int ultimoAnio = historial.lastKey();
        Map<Integer, Double> pronostico = precios.predictMultiYear(nombre, ultimoAnio + 1, anios);

        // Historial: variación de cada año frente al anterior, junto a la inflación de ese año.
        List<Map<String, Object>> historico = new ArrayList<>();
        Double anterior = null;
        for (Map.Entry<Integer, Double> e : historial.entrySet()) {
            Map<String, Object> punto = new LinkedHashMap<>();
            punto.put("anio", e.getKey());
            punto.put("precio", redondear(e.getValue()));
            punto.put("variacionPct", anterior == null ? null : variacionPct(e.getValue(), anterior));
            punto.put("ipcNacionalPct", InflacionDane.NACIONAL.get(e.getKey()));
            punto.put("ipcCartagenaPct", InflacionDane.CARTAGENA.get(e.getKey()));
            historico.add(punto);
            anterior = e.getValue();
        }

        // Pronóstico: cada año frente al anterior y contra la inflación (publicada, o la última publicada).
        List<Map<String, Object>> futuro = new ArrayList<>();
        double previo = historial.get(ultimoAnio);
        for (int i = 0; i < anios; i++) {
            int anio = ultimoAnio + 1 + i;
            Double valor = pronostico.get(anio);
            if (valor == null || valor.isNaN() || valor.isInfinite()) {
                throw new IllegalStateException("El modelo no pudo calcular el pronóstico de " + nombre + " para " + anio);
            }
            Map<String, Object> punto = puntoPronostico(anio, valor, previo);
            double[] intervalo = precios.predictInterval(nombre, anio);
            if (intervalo != null && intervalo.length == 2) {
                punto.put("minimo", redondear(intervalo[0]));
                punto.put("maximo", redondear(intervalo[1]));
            }
            futuro.add(punto);
            previo = valor;
        }

        Map<String, Object> d = new LinkedHashMap<>();
        d.put("producto", nombre);
        d.put("historico", historico);
        d.put("pronostico", futuro);
        d.put("resumen", resumen(nombre, futuro.get(0)));
        Map<String, Object> ficha = fichaModelo();
        if (!ficha.isEmpty()) {
            d.put("modelo", ficha);
        }
        Map<String, Object> fuentes = new LinkedHashMap<>();
        fuentes.put("precios", "Dataset de productos de consumo masivo (Kaggle), precios por año");
        fuentes.put("inflacion", InflacionDane.FUENTE);
        d.put("fuentes", fuentes);
        return MobileApiResponse.ok(d);
    }

    // ── GET /api/prediccion/modelo ────────────────────────────────────────────

    /** Ficha completa del modelo, con la tabla comparativa de los modelos evaluados. */
    public Map<String, Object> modelo() {
        Map<String, Object> info = precios.getModelInfo();
        if (info == null || info.isEmpty()) {
            throw MobileException.notFound("No hay información del modelo de predicción");
        }
        return MobileApiResponse.ok(new LinkedHashMap<>(info));
    }

    /** Resumen del modelo para acompañar cada pronóstico (sin la tabla comparativa). */
    private Map<String, Object> fichaModelo() {
        Map<String, Object> info = precios.getModelInfo();
        Map<String, Object> ficha = new LinkedHashMap<>();
        if (info == null) {
            return ficha;
        }
        for (String campo : new String[] { "modelo", "tipoProblema", "variablePredictiva", "variablesPredictoras",
                "validacion", "metricas" }) {
            if (info.get(campo) != null) {
                ficha.put(campo, info.get(campo));
            }
        }
        return ficha;
    }

    // ── GET /api/prediccion/panorama ──────────────────────────────────────────

    /**
     * Pronóstico del primer año futuro de todos los productos del modelo, comparado con la
     * inflación. Alimenta los gráficos comparativos de la app (radar, treemap y ranking).
     */
    public Map<String, Object> panorama() {
        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> referencia = null;
        int porEncima = 0;
        int enLinea = 0;
        int porDebajo = 0;

        for (String nombre : precios.getProductCategories()) {
            TreeMap<Integer, Double> historial = new TreeMap<>(precios.getHistory(nombre));
            if (historial.isEmpty()) {
                continue;
            }
            int ultimoAnio = historial.lastKey();
            Double valor = precios.predictMultiYear(nombre, ultimoAnio + 1, 1).get(ultimoAnio + 1);
            if (valor == null || valor.isNaN() || valor.isInfinite()) {
                continue; // un producto sin pronóstico no debe tumbar todo el panorama
            }
            Map<String, Object> punto = puntoPronostico(ultimoAnio + 1, valor, historial.get(ultimoAnio));
            if (referencia == null) {
                referencia = punto;
            }
            double variacion = (Double) punto.get("variacionPct");
            Double cartagena = (Double) punto.get("ipcCartagenaPct");
            double base = cartagena != null ? cartagena : (Double) punto.get("ipcNacionalPct");
            String veredicto = (String) punto.get("veredicto");
            switch (veredicto) {
                case POR_ENCIMA -> porEncima++;
                case POR_DEBAJO -> porDebajo++;
                default -> enLinea++;
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("nombre", nombre);
            item.put("ultimoPrecio", redondear(historial.get(ultimoAnio)));
            item.put("precioPronosticado", punto.get("precio"));
            item.put("variacionPct", variacion);
            item.put("diferenciaPuntos", redondear(variacion - base));
            item.put("veredicto", veredicto);
            items.add(item);
        }
        if (referencia == null) {
            throw MobileException.notFound("No hay historial de precios para comparar");
        }
        items.sort((a, b) -> Double.compare((Double) b.get("diferenciaPuntos"), (Double) a.get("diferenciaPuntos")));

        Map<String, Object> conteo = new LinkedHashMap<>();
        conteo.put("porEncima", porEncima);
        conteo.put("enLinea", enLinea);
        conteo.put("porDebajo", porDebajo);

        Map<String, Object> d = new LinkedHashMap<>();
        d.put("anio", referencia.get("anio"));
        d.put("ipcNacionalPct", referencia.get("ipcNacionalPct"));
        d.put("ipcCartagenaPct", referencia.get("ipcCartagenaPct"));
        d.put("ipcEsReferencia", referencia.get("ipcEsReferencia"));
        d.put("conteo", conteo);
        d.put("productos", items);
        Map<String, Object> fuentes = new LinkedHashMap<>();
        fuentes.put("precios", "Dataset de productos de consumo masivo (Kaggle), precios por año");
        fuentes.put("inflacion", InflacionDane.FUENTE);
        d.put("fuentes", fuentes);
        return MobileApiResponse.ok(d);
    }

    // ── Piezas ────────────────────────────────────────────────────────────────

    private Map<String, Object> puntoPronostico(int anio, double valor, double previo) {
        double variacion = variacionPct(valor, previo);

        Double nacional = InflacionDane.NACIONAL.get(anio);
        Double cartagena = InflacionDane.CARTAGENA.get(anio);
        boolean referencia = nacional == null;
        Integer anioReferencia = null;
        if (referencia) {
            nacional = InflacionDane.ultimo(InflacionDane.NACIONAL);
            anioReferencia = InflacionDane.ultimoAnio(InflacionDane.NACIONAL);
        }
        if (cartagena == null) {
            cartagena = InflacionDane.ultimo(InflacionDane.CARTAGENA);
        }

        // La referencia principal es Cartagena (inflación regional); si no hay, la nacional.
        double base = cartagena != null ? cartagena : nacional;

        Map<String, Object> p = new LinkedHashMap<>();
        p.put("anio", anio);
        p.put("precio", redondear(valor));
        p.put("variacionPct", variacion);
        p.put("ipcNacionalPct", nacional);
        p.put("ipcCartagenaPct", cartagena);
        p.put("ipcEsReferencia", referencia);
        p.put("anioReferencia", anioReferencia);
        p.put("veredicto", veredicto(variacion, base));
        return p;
    }

    private Map<String, Object> resumen(String producto, Map<String, Object> primero) {
        int anio = (Integer) primero.get("anio");
        double variacion = (Double) primero.get("variacionPct");
        Double nacional = (Double) primero.get("ipcNacionalPct");
        Double cartagena = (Double) primero.get("ipcCartagenaPct");
        boolean referencia = (Boolean) primero.get("ipcEsReferencia");
        String veredicto = (String) primero.get("veredicto");

        double base = cartagena != null ? cartagena : nacional;
        String lugar = cartagena != null ? "Cartagena" : "el país";

        StringBuilder m = new StringBuilder();
        m.append(String.format(ES_CO, "Para %d el modelo proyecta que %s cambie %s en el precio, frente a una inflación de %s en %s",
                anio, producto, conSigno(variacion), porcentaje(base), lugar));
        if (referencia) {
            m.append(" (último dato publicado)");
        }
        m.append(". ");
        switch (veredicto) {
            case POR_ENCIMA -> m.append("Sube más que la inflación.");
            case POR_DEBAJO -> m.append(variacion < 0 ? "Baja mientras los precios en general suben." : "Sube menos que la inflación.");
            default -> m.append("Va a la par de la inflación.");
        }

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("anio", anio);
        r.put("variacionPronosticoPct", variacion);
        r.put("ipcNacionalPct", nacional);
        r.put("ipcCartagenaPct", cartagena);
        r.put("ipcEsReferencia", referencia);
        r.put("diferenciaNacionalPuntos", redondear(variacion - nacional));
        r.put("diferenciaCartagenaPuntos", cartagena == null ? null : redondear(variacion - cartagena));
        r.put("veredicto", veredicto);
        r.put("mensaje", m.toString());
        return r;
    }

    static String veredicto(double variacionProducto, double inflacionReferencia) {
        double diferencia = variacionProducto - inflacionReferencia;
        if (diferencia > UMBRAL_PUNTOS) {
            return POR_ENCIMA;
        }
        if (diferencia < -UMBRAL_PUNTOS) {
            return POR_DEBAJO;
        }
        return EN_LINEA;
    }

    static double variacionPct(double actual, double anterior) {
        if (anterior == 0) {
            return 0;
        }
        return redondear((actual / anterior - 1) * 100);
    }

    static double redondear(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private static String porcentaje(double v) {
        return String.format(ES_CO, "%.1f %%", v);
    }

    private static String conSigno(double v) {
        return String.format(ES_CO, "%+.1f %%", v);
    }

    private static int parsearAnios(String texto) {
        if (texto == null || texto.isBlank()) {
            return ANIOS_POR_DEFECTO;
        }
        try {
            int n = Integer.parseInt(texto.trim());
            if (n >= 1 && n <= ANIOS_MAXIMO) {
                return n;
            }
        } catch (NumberFormatException ignorado) {
            // cae al error de abajo
        }
        throw MobileException.badRequest("El parámetro anios debe ser un número entre 1 y " + ANIOS_MAXIMO);
    }
}
