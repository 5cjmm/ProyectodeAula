package com.ShopMaster.dto;

public class OptimizacionRequest {

    private String tiendaId;           // para filtrar productos de MongoDB
    private double inventarioTotal;
    private double presupuesto;
    private double creditoDisponible;
    private double capacidadVentas;

    public String getTiendaId()                      { return tiendaId; }
    public void setTiendaId(String tiendaId)         { this.tiendaId = tiendaId; }

    public double getInventarioTotal()               { return inventarioTotal; }
    public void setInventarioTotal(double v)         { this.inventarioTotal = v; }

    public double getPresupuesto()                   { return presupuesto; }
    public void setPresupuesto(double v)             { this.presupuesto = v; }

    public double getCreditoDisponible()             { return creditoDisponible; }
    public void setCreditoDisponible(double v)       { this.creditoDisponible = v; }

    public double getCapacidadVentas()               { return capacidadVentas; }
    public void setCapacidadVentas(double v)         { this.capacidadVentas = v; }
}
