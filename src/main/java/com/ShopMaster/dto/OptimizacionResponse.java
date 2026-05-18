package com.ShopMaster.dto;

import java.util.List;
import java.util.Map;

public class OptimizacionResponse {

    private String estado;                          // "optimo" | "sin_solucion" | "sin_productos"
    private String mensaje;                         // mensaje de error si no hay solución
    private double gananciaOptima;
    private List<Map<String, Object>> productos;    // lista con nombre, cantidad, ganancia de cada producto
    private Map<String, Double> usoRestricciones;   // % de uso de inventario, cap.ventas, presupuesto, crédito

    public String getEstado()                                        { return estado; }
    public void setEstado(String estado)                             { this.estado = estado; }

    public String getMensaje()                                       { return mensaje; }
    public void setMensaje(String mensaje)                           { this.mensaje = mensaje; }

    public double getGananciaOptima()                                { return gananciaOptima; }
    public void setGananciaOptima(double gananciaOptima)             { this.gananciaOptima = gananciaOptima; }

    public List<Map<String, Object>> getProductos()                  { return productos; }
    public void setProductos(List<Map<String, Object>> productos)    { this.productos = productos; }

    public Map<String, Double> getUsoRestricciones()                 { return usoRestricciones; }
    public void setUsoRestricciones(Map<String, Double> u)           { this.usoRestricciones = u; }
}
