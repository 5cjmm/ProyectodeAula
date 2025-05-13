package com.ShopMaster.Model;

import org.bson.types.ObjectId;

public class ProductoVendido {
    private ObjectId productoId;
    private String codigo;
    private String nombre;
    private int cantidad;
    private double precio;

    public ProductoVendido() {}


    public ProductoVendido(ObjectId productoId, String codigo, String nombre, int cantidad, double precio) {
        this.productoId = productoId;
        this.codigo = codigo;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.precio = precio;
    }


    public ObjectId getProductoId() {
        return this.productoId;
    }

    public void setProductoId(ObjectId productoId) {
        this.productoId = productoId;
    }

    public String getCodigo() {
        return this.codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return this.nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getCantidad() {
        return this.cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getPrecio() {
        return this.precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }
    
}

