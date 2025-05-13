package com.ShopMaster.Model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "deudas")
public class Deuda {

    @Id
    private String id;
    private String cedulaCliente;
    private String nombreCliente;
    private List<ProductoVendido> productos;
    private double total;
    private double totalRestante;
    private String estado; // Ej: NO PAGADA, PARCIAL, PAGADA
    private LocalDateTime fechaVenta;
    private List<Abono> historialAbonos = new ArrayList<>();

    public Deuda() {
    }

    // Constructor con parámetros principales
    public Deuda(String cedulaCliente, String nombreCliente, List<ProductoVendido> productos,
                 double total, LocalDateTime fechaVenta) {
        this.cedulaCliente = cedulaCliente;
        this.nombreCliente = nombreCliente;
        this.productos = productos;
        this.total = total;
        this.totalRestante = total;
        this.estado = "NO PAGADA";
        this.fechaVenta = fechaVenta;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCedulaCliente() {
        return cedulaCliente;
    }

    public void setCedulaCliente(String cedulaCliente) {
        this.cedulaCliente = cedulaCliente;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public List<ProductoVendido> getProductos() {
        return productos;
    }

    public void setProductos(List<ProductoVendido> productos) {
        this.productos = productos;
    }


    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getTotalRestante() {
        return totalRestante;
    }

    public void setTotalRestante(double totalRestante) {
        this.totalRestante = totalRestante;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaVenta() {
        return fechaVenta;
    }

    public void setFechaVenta(LocalDateTime fechaVenta) {
        this.fechaVenta = fechaVenta;
    }

    public List<Abono> getHistorialAbonos() {
        return historialAbonos;
    }

    public void setHistorialAbonos(List<Abono> historialAbonos) {
        this.historialAbonos = historialAbonos;
    }

    public void registrarAbono(double monto) {
        this.totalRestante -= monto;
        if (this.totalRestante <= 0) {
            this.totalRestante = 0;
            this.estado = "PAGADA";
        } else {
            this.estado = "PARCIAL";
        }
        this.historialAbonos.add( new Abono(monto, LocalDateTime.now()));
    }
}