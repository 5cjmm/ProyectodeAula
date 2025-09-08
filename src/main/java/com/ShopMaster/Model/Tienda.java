package com.ShopMaster.Model;

public class Tienda {
    private String id;
    private String nombre;
    private String direccion;
    private String tipo;
    private String nit;


    public Tienda() {
    }


    public Tienda(String id, String nombre, String direccion, String tipo, String nit) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.tipo = tipo;
        this.nit = nit;
    }


    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return this.nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return this.direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTipo() {
        return this.tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getNit() {
        return this.nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

}