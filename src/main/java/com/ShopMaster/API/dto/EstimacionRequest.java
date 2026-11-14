package com.ShopMaster.API.dto;

/** Datos del producto cuyo precio se quiere estimar con la red neuronal. */
public class EstimacionRequest {
    private String subcategoria;
    private String marca;
    private String unidadVenta;
    private Double cantidad;
    private String unidadMedida;

    public String getSubcategoria() { return subcategoria; }
    public void setSubcategoria(String subcategoria) { this.subcategoria = subcategoria; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getUnidadVenta() { return unidadVenta; }
    public void setUnidadVenta(String unidadVenta) { this.unidadVenta = unidadVenta; }
    public Double getCantidad() { return cantidad; }
    public void setCantidad(Double cantidad) { this.cantidad = cantidad; }
    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }
}
