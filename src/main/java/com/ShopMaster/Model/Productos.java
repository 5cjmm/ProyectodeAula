package com.ShopMaster.Model;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "productos")
public class Productos {

    @Id
    private String id;

    @Indexed(unique = true)
    private String codigo;

    private String nombre;
    private int cantidad;
    private double precio;
    private String tiendaId;
    private List<ObjectId> proveedorIds;

    @Transient // 👈 no se guarda en Mongo
    private List<String> proveedorIdStrs;


    public Productos() {
    }


    public Productos(String id, String codigo, String nombre, int cantidad, double precio, String tiendaId, List<ObjectId> proveedorIds) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.precio = precio;
        this.tiendaId = tiendaId;
        this.proveedorIds = proveedorIds;
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

    public String getTiendaId() {
        return this.tiendaId;
    }

    public void setTiendaId(String tiendaId) {
        this.tiendaId = tiendaId;
    }


    public List<ObjectId> getProveedorIds() {
        return this.proveedorIds;
    }

    public void setProveedorIds(List<ObjectId> proveedorIds) {
        this.proveedorIds = proveedorIds;
    }

    

    public List<String> getProveedorIdStrs() {
        return this.proveedorIdStrs;
    }

    public void setProveedorIdStrs(List<String> proveedorIdStrs) {
        this.proveedorIdStrs = proveedorIdStrs;
    }

    
}