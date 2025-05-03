package com.ShopMaster.Model;

import java.util.Date;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "deudas")
public class Deuda {
    @Id
    private String id;
    private String cliente;
    private Double montoAdeudado;
    private Set<String> estado;
    private Date fechaDeRegistro;

    public Deuda() {
    }


    public Deuda(String id, String cliente, Double montoAdeudado, Set<String> estado, Date fechaDeRegistro) {
        this.id = id;
        this.cliente = cliente;
        this.montoAdeudado = montoAdeudado;
        this.estado = estado;
        this.fechaDeRegistro = fechaDeRegistro;
    }


    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCliente() {
        return this.cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public Double getMontoAdeudado() {
        return this.montoAdeudado;
    }

    public void setMontoAdeudado(Double montoAdeudado) {
        this.montoAdeudado = montoAdeudado;
    }

    public Set<String> getEstado() {
        return this.estado;
    }

    public void setEstado(Set<String> estado) {
        this.estado = estado;
    }

    public Date getFechaDeRegistro() {
        return this.fechaDeRegistro;
    }

    public void setFechaDeRegistro(Date fechaDeRegistro) {
        this.fechaDeRegistro = fechaDeRegistro;
    }


}
