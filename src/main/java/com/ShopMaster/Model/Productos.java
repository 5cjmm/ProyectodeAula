package com.ShopMaster.Model;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "productos")
public class Productos {

    @Id
    private ObjectId  id;
    private String codigo;
    private String nombre;
    private int cantidad;
    private double precio;
    private List<ObjectId> proveedorIds;


    public Productos() {
    }


    public Productos(ObjectId id, String codigo, String nombre, int cantidad, double precio, List<ObjectId> proveedorIds) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.precio = precio;
        this.proveedorIds = proveedorIds;
    }


    public ObjectId getId() {
        return this.id;
    }

    public void setId(ObjectId id) {
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

    

    
}
