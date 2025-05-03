package com.ShopMaster.Model;

import java.util.List;

import org.bson.types.ObjectId;

public class ProductoConProveedores {

    private String id;
    private String codigo;
    private String nombre;
    private int cantidad;
    private double precio;
    private List<ObjectId> proveedorIds;
    private List<Proveedor> proveedores;


    public ProductoConProveedores() {
    }


    public ProductoConProveedores(String id, String codigo, String nombre, int cantidad, double precio, List<ObjectId> proveedorIds, List<Proveedor> proveedores) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.precio = precio;
        this.proveedorIds = proveedorIds;
        this.proveedores = proveedores;
    }


    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
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

    public List<ObjectId> getProveedorIds() {
        return this.proveedorIds;
    }

    public void setProveedorIds(List<ObjectId> proveedorIds) {
        this.proveedorIds = proveedorIds;
    }

    public List<Proveedor> getProveedores() {
        return this.proveedores;
    }

    public void setProveedores(List<Proveedor> proveedores) {
        this.proveedores = proveedores;
    }

    

}
 
    


