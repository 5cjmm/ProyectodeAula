package com.ShopMaster.Mobile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.ShopMaster.API.service.PricePredictionService;

class MobilePrediccionServiceTest {

    private static final String ARROZ = "Arroz Diana x 500g";
    private static final double[] PRECIOS_ARROZ = {
            5339.21, 5789.8, 6338.65, 6766.55, 7268.69, 7962.89, 8275.53, 8939.42, 9641.73, 10417.77 };

    private PricePredictionService modelo;
    private MobilePrediccionService servicio;

    @BeforeEach
    void preparar() {
        modelo = mock(PricePredictionService.class);
        servicio = new MobilePrediccionService(modelo);

        TreeMap<Integer, Double> historial = new TreeMap<>();
        for (int i = 0; i < PRECIOS_ARROZ.length; i++) {
            historial.put(2015 + i, PRECIOS_ARROZ[i]);
        }
        when(modelo.getProductCategories()).thenReturn(List.of(ARROZ, "Leche Colanta 1L", "unknown"));
        when(modelo.getHistory(ARROZ)).thenReturn(historial);
        when(modelo.getHistory("Leche Colanta 1L")).thenReturn(new TreeMap<>(Map.of(2024, 4300.0)));
        when(modelo.getHistory("unknown")).thenReturn(new TreeMap<>());

        Map<Integer, Double> pronostico = new LinkedHashMap<>();
        pronostico.put(2025, 11200.0);
        pronostico.put(2026, 12000.0);
        pronostico.put(2027, 12800.0);
        when(modelo.predictMultiYear(ARROZ, 2025, 3)).thenReturn(pronostico);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> data(Map<String, Object> r) {
        return (Map<String, Object>) r.get("data");
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> lista(Object o) {
        return (List<Map<String, Object>>) o;
    }

    @Test
    void laListaDeProductosTraeElUltimoPrecioYOmiteLosQueNoTienenHistorial() {
        List<Map<String, Object>> productos = lista(servicio.productos().get("data"));
        assertEquals(2, productos.size());
        assertEquals(ARROZ, productos.get(0).get("nombre"));
        assertEquals(2024, productos.get(0).get("ultimoAnio"));
        assertEquals(10417.77, (Double) productos.get(0).get("ultimoPrecio"), 0.001);
    }

    @Test
    void elHistorialTraeLaVariacionAnualYLaInflacionDeCadaAnio() {
        List<Map<String, Object>> historico = lista(data(servicio.precios(ARROZ, null)).get("historico"));
        assertEquals(10, historico.size());

        Map<String, Object> primero = historico.get(0);
        assertEquals(2015, primero.get("anio"));
        assertNull(primero.get("variacionPct"));

        Map<String, Object> y2016 = historico.get(1);
        assertEquals(8.44, (Double) y2016.get("variacionPct"), 0.001);
        assertEquals(5.75, (Double) y2016.get("ipcNacionalPct"), 0.001);
        assertNull(y2016.get("ipcCartagenaPct"), "no hay dato de Cartagena para 2016");

        Map<String, Object> y2022 = historico.get(7);
        assertEquals(13.12, (Double) y2022.get("ipcNacionalPct"), 0.001);
        assertEquals(14.33, (Double) y2022.get("ipcCartagenaPct"), 0.001);
    }

    @Test
    void elPronosticoArrancaDespuesDelUltimoAnioYSeCompara() {
        List<Map<String, Object>> futuro = lista(data(servicio.precios(ARROZ, null)).get("pronostico"));
        assertEquals(3, futuro.size());

        Map<String, Object> y2025 = futuro.get(0);
        assertEquals(2025, y2025.get("anio"));
        assertEquals(7.51, (Double) y2025.get("variacionPct"), 0.001);
        assertEquals(5.10, (Double) y2025.get("ipcNacionalPct"), 0.001);
        assertEquals(4.80, (Double) y2025.get("ipcCartagenaPct"), 0.001);
        assertEquals(false, y2025.get("ipcEsReferencia"));
        assertEquals(MobilePrediccionService.POR_ENCIMA, y2025.get("veredicto"));

        // 2026 todavía no tiene inflación anual publicada: se usa el último dato como referencia.
        Map<String, Object> y2026 = futuro.get(1);
        assertEquals(7.14, (Double) y2026.get("variacionPct"), 0.001);
        assertEquals(true, y2026.get("ipcEsReferencia"));
        assertEquals(2025, y2026.get("anioReferencia"));
        assertEquals(5.10, (Double) y2026.get("ipcNacionalPct"), 0.001);
    }

    @Test
    void elResumenDiceSiSubeMasQueLaInflacion() {
        Map<String, Object> resumen = (Map<String, Object>) data(servicio.precios(ARROZ, "3")).get("resumen");
        assertEquals(2025, resumen.get("anio"));
        assertEquals(2.71, (Double) resumen.get("diferenciaCartagenaPuntos"), 0.001);
        assertEquals(2.41, (Double) resumen.get("diferenciaNacionalPuntos"), 0.001);
        assertEquals(MobilePrediccionService.POR_ENCIMA, resumen.get("veredicto"));
        String mensaje = (String) resumen.get("mensaje");
        assertTrue(mensaje.contains("Sube más que la inflación"), mensaje);
        assertTrue(mensaje.contains(ARROZ), mensaje);
    }

    @Test
    void elVeredictoUsaUnMargenDeUnPunto() {
        assertEquals(MobilePrediccionService.EN_LINEA, MobilePrediccionService.veredicto(6.0, 5.0));
        assertEquals(MobilePrediccionService.EN_LINEA, MobilePrediccionService.veredicto(4.0, 5.0));
        assertEquals(MobilePrediccionService.POR_ENCIMA, MobilePrediccionService.veredicto(6.01, 5.0));
        assertEquals(MobilePrediccionService.POR_DEBAJO, MobilePrediccionService.veredicto(3.99, 5.0));
        assertEquals(MobilePrediccionService.POR_DEBAJO, MobilePrediccionService.veredicto(-2.0, 5.0));
    }

    @Test
    void elNombreDelProductoNoDistingueMayusculas() {
        assertEquals(ARROZ, data(servicio.precios("  arroz diana X 500G ", null)).get("producto"));
    }

    @Test
    void validaLosParametros() {
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(MobileException.class, () -> servicio.precios("  ", null)).getStatus());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(MobileException.class, () -> servicio.precios(null, null)).getStatus());
        assertEquals(HttpStatus.NOT_FOUND, assertThrows(MobileException.class, () -> servicio.precios("Papa criolla", null)).getStatus());
        for (String malo : new String[] { "0", "6", "-1", "abc", "2.5" }) {
            MobileException e = assertThrows(MobileException.class, () -> servicio.precios(ARROZ, malo));
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus(), malo);
        }
    }

    @Test
    void unProductoSinHistorialDa404() {
        assertEquals(HttpStatus.NOT_FOUND, assertThrows(MobileException.class, () -> servicio.precios("unknown", null)).getStatus());
    }

    @Test
    void siElModeloDevuelveNaNNoSeInventaUnPronostico() {
        Map<Integer, Double> roto = new LinkedHashMap<>();
        roto.put(2025, Double.NaN);
        when(modelo.predictMultiYear(ARROZ, 2025, 1)).thenReturn(roto);
        assertThrows(IllegalStateException.class, () -> servicio.precios(ARROZ, "1"));
    }

    @Test
    void laTablaDelDaneEstaCompleta() {
        for (int anio = 2015; anio <= 2025; anio++) {
            assertTrue(InflacionDane.NACIONAL.containsKey(anio), "falta el IPC nacional de " + anio);
        }
        assertFalse(InflacionDane.CARTAGENA.containsKey(2019), "no se inventan datos de Cartagena 2015-2019");
        assertEquals(2025, InflacionDane.ultimoAnio(InflacionDane.NACIONAL));
    }

    @Test
    void panoramaComparaTodosLosProductosConElIpc() {
        when(modelo.predictMultiYear(ARROZ, 2025, 1)).thenReturn(Map.of(2025, 11200.0));
        when(modelo.predictMultiYear("Leche Colanta 1L", 2025, 1)).thenReturn(Map.of(2025, 4300.0 * 1.02));

        Map<String, Object> d = data(servicio.panorama());
        assertEquals(2025, d.get("anio"));
        assertEquals(5.10, (Double) d.get("ipcNacionalPct"), 1e-9);
        assertEquals(4.80, (Double) d.get("ipcCartagenaPct"), 1e-9);

        List<Map<String, Object>> productos = lista(d.get("productos"));
        assertEquals(2, productos.size()); // "unknown" no tiene historial
        // Ordenado de mayor a menor diferencia frente al IPC: el arroz (+7.5 %) sube más que la leche (+2 %).
        assertEquals(ARROZ, productos.get(0).get("nombre"));
        assertEquals("POR_ENCIMA", productos.get(0).get("veredicto"));
        assertEquals("POR_DEBAJO", productos.get(1).get("veredicto"));
        assertEquals(2.71, (Double) productos.get(0).get("diferenciaPuntos"), 1e-9);

        @SuppressWarnings("unchecked")
        Map<String, Object> conteo = (Map<String, Object>) d.get("conteo");
        assertEquals(1, conteo.get("porEncima"));
        assertEquals(0, conteo.get("enLinea"));
        assertEquals(1, conteo.get("porDebajo"));
    }

    @Test
    void incluyeIntervaloYFichaDelModeloCuandoExisten() {
        when(modelo.predictInterval(ARROZ, 2025)).thenReturn(new double[] { 10600.0, 11800.0 });
        when(modelo.getModelInfo()).thenReturn(Map.of(
                "modelo", "Regresión log-lineal por producto",
                "variablePredictiva", "precio",
                "variablesPredictoras", List.of("año", "producto"),
                "metricas", Map.of("porcentajePredictivo", 97.85, "r2", 0.9948),
                "comparativa", List.of(Map.of("modelo", "otro"))));

        Map<String, Object> d = data(servicio.precios(ARROZ, "3"));
        Map<String, Object> primero = lista(d.get("pronostico")).get(0);
        assertEquals(10600.0, (Double) primero.get("minimo"), 1e-9);
        assertEquals(11800.0, (Double) primero.get("maximo"), 1e-9);
        assertFalse(lista(d.get("pronostico")).get(1).containsKey("minimo")); // el modelo no dio intervalo de 2026

        @SuppressWarnings("unchecked")
        Map<String, Object> ficha = (Map<String, Object>) d.get("modelo");
        assertEquals("precio", ficha.get("variablePredictiva"));
        assertTrue(ficha.containsKey("metricas"));
        assertFalse(ficha.containsKey("comparativa")); // la comparativa va solo en /modelo

        assertTrue(data(servicio.modelo()).containsKey("comparativa"));
    }

    @Test
    void sinInformacionDelModeloNoHayFichaYModeloDa404() {
        Map<String, Object> d = data(servicio.precios(ARROZ, "3"));
        assertFalse(d.containsKey("modelo"));
        MobileException e = assertThrows(MobileException.class, () -> servicio.modelo());
        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
    }
}
