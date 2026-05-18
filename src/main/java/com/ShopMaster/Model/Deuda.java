package com.ShopMaster.Model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "deudas")
public class Deuda {

    @Id
    private String id;

    @Indexed(unique = true)
    private String cedulaCliente;

    private String nombreCliente;
    private List<ProductoVendido> productos;
    private double total;
    private double totalRestante;
    private String estado;
    private LocalDateTime fechaVenta;
    private String tiendaId;
    private List<Abono> historialAbonos = new ArrayList<>();

    // ── Soft Delete ──────────────────────────────
    private boolean activo = true;

    public Deuda() {}

    public Deuda(String cedulaCliente, String nombreCliente, List<ProductoVendido> productos,
                 double total, LocalDateTime fechaVenta, String tiendaId) {
        this.cedulaCliente  = cedulaCliente;
        this.nombreCliente  = nombreCliente;
        this.productos      = productos;
        this.total          = total;
        this.totalRestante  = total;
        this.estado         = "NO PAGADA";
        this.tiendaId       = tiendaId;
        this.fechaVenta     = fechaVenta;
        this.activo         = true;
    }

    // Getters
    public String              getId()              { return id; }
    public String              getCedulaCliente()   { return cedulaCliente; }
    public String              getNombreCliente()   { return nombreCliente; }
    public List<ProductoVendido> getProductos()     { return productos; }
    public double              getTotal()           { return total; }
    public double              getTotalRestante()   { return totalRestante; }
    public String              getEstado()          { return estado; }
    public LocalDateTime       getFechaVenta()      { return fechaVenta; }
    public String              getTiendaId()        { return tiendaId; }
    public List<Abono>         getHistorialAbonos() { return historialAbonos; }
    public boolean             isActivo()           { return activo; }

    // Setters
    public void setId(String id)                              { this.id              = id; }
    public void setCedulaCliente(String cedulaCliente)        { this.cedulaCliente   = cedulaCliente; }
    public void setNombreCliente(String nombreCliente)        { this.nombreCliente   = nombreCliente; }
    public void setProductos(List<ProductoVendido> productos) { this.productos       = productos; }
    public void setTotal(double total)                        { this.total           = total; }
    public void setTotalRestante(double totalRestante)        { this.totalRestante   = totalRestante; }
    public void setEstado(String estado)                      { this.estado          = estado; }
    public void setFechaVenta(LocalDateTime fechaVenta)       { this.fechaVenta      = fechaVenta; }
    public void setTiendaId(String tiendaId)                  { this.tiendaId        = tiendaId; }
    public void setHistorialAbonos(List<Abono> historial)     { this.historialAbonos = historial; }
    public void setActivo(boolean activo)                     { this.activo          = activo; }

    public void registrarAbono(double monto, LocalDateTime fecha) {
        this.totalRestante -= monto;
        if (this.totalRestante <= 0) {
            this.totalRestante = 0;
            this.estado = "PAGADA";
        } else {
            this.estado = "PARCIAL";
        }
        this.historialAbonos.add(new Abono(monto, fecha));
    }
}
