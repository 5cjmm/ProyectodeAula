package com.ShopMaster.API.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

class ModeloRedPreciosTest {

    private final ModeloRedPrecios modelo = new ModeloRedPrecios();

    @Test
    void reproduceLasEstimacionesDePython() {
        assertTrue(modelo.disponible());
        JsonNode casos = modelo.casosVerificacion();
        assertTrue(casos.size() >= 3);
        for (JsonNode c : casos) {
            Double cantidad = c.get("cantidad").isNull() ? null : c.get("cantidad").asDouble();
            Map<String, Object> r = modelo.estimar(c.get("subcategoria").asText(), c.get("marca").asText(),
                    c.get("unidadVenta").asText(), cantidad, c.get("unidadMedida").asText());
            assertEquals(c.get("precioEstimado").asDouble(), (Double) r.get("precioEstimado"), 0.5,
                    "Distinto de Python para " + c);
        }
    }

    @Test
    void elRangoRodeaLaEstimacionYLaMarcaDesconocidaCaeEnOtra() {
        Map<String, Object> r = modelo.estimar("Despensa", "MARCA-INEXISTENTE", "Unidades", 500.0, "g");
        double p = (Double) r.get("precioEstimado");
        assertTrue((Double) r.get("minimo") < p && p < (Double) r.get("maximo"));
        assertEquals(p, (Double) modelo.estimar("Despensa", "OTRA", "Unidades", 500.0, "g").get("precioEstimado"), 1e-9);
    }

    @Test
    void masCantidadCuestaMasYSinCantidadFunciona() {
        double chico = (Double) modelo.estimar("Despensa", "OTRA", "Unidades", 250.0, "g").get("precioEstimado");
        double grande = (Double) modelo.estimar("Despensa", "OTRA", "Unidades", 2000.0, "g").get("precioEstimado");
        assertTrue(grande > chico);
        modelo.estimar("Despensa", "OTRA", "Unidades", null, "ninguna");
    }

    @Test
    void validaLosDatosDeEntrada() {
        assertThrows(IllegalArgumentException.class, () -> modelo.estimar(null, "OTRA", "Unidades", 1.0, "g"));
        assertThrows(IllegalArgumentException.class, () -> modelo.estimar("No existe", "OTRA", "Unidades", 1.0, "g"));
        assertThrows(IllegalArgumentException.class, () -> modelo.estimar("Despensa", "OTRA", "Caja", 1.0, "g"));
        assertThrows(IllegalArgumentException.class, () -> modelo.estimar("Despensa", "OTRA", "Unidades", 1.0, "libras"));
    }
}
