package com.ShopMaster.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "proveedores")
public class Proveedor {
    
    @Id
    private String id;
    private String nombre;

    @Indexed(unique = true)
    private String nit;

    private String direccion;
    
    @Indexed(unique = true)
    private String telefono;

    private String tiendaId;

    private boolean activo = true;  // false = eliminado lógicamente
    

    public Proveedor() {
    }

    public Proveedor(String id, String nombre, String nit, String direccion,
                     String telefono, String tiendaId) {
        this.id = id;
        this.nombre = nombre;
        this.nit = nit;
        this.direccion = direccion;
        this.telefono = telefono;
        this.tiendaId = tiendaId;
        this.activo = true;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getNit() { return nit; }
    public void setNit(String nit) { this.nit = nit; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getTiendaId() { return tiendaId; }
    public void setTiendaId(String tiendaId) { this.tiendaId = tiendaId; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}


//    public Proveedor(String id, String nombre, String nit, String direccion, String telefono, String tiendaId) {
//        this.id = id;
//        this.nombre = nombre;
//        this.nit = nit;
//        this.direccion = direccion;
//        this.telefono = telefono;
//        this.tiendaId = tiendaId;
//    }
//
//
//    public String getId() {
//        return this.id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
//
//
//    public String getNombre() {
//        return this.nombre;
//    }
//
//    public void setNombre(String nombre) {
//        this.nombre = nombre;
//    }
//
//    public String getNit() {
//        return this.nit;
//    }
//
//    public void setNit(String nit) {
//        this.nit = nit;
//    }
//
//    public String getDireccion() {
//        return this.direccion;
//    }
//
//    public void setDireccion(String direccion) {
//        this.direccion = direccion;
//    }
//
//    public String getTelefono() {
//        return this.telefono;
//    }
//
//    public void setTelefono(String telefono) {
//        this.telefono = telefono;
//    }
//
//    public String getTiendaId() {
//        return this.tiendaId;
//    }
//
//    public void setTiendaId(String tiendaId) {
//        this.tiendaId = tiendaId;
//    }
//
//}