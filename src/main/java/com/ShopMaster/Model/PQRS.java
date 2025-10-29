package com.ShopMaster.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "pqrs")
public class PQRS {
    @Id
    private String id;
    private String tipo;          // "Sugerencia" o "Reporte de Fallas"
    private String nombre;
    private String email;
    private String asunto;
    private String descripcion;
    private LocalDateTime fechaEnvio = LocalDateTime.now();


    public PQRS() {
    }

    public PQRS(String tipo, String nombre, String email, String asunto, String descripcion) {
        this.tipo = tipo;
        this.nombre = nombre;
        this.email = email;
        this.asunto = asunto;
        this.descripcion = descripcion;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTipo() {
        return this.tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getNombre() {
        return this.nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAsunto() {
        return this.asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getDescripcion() {
        return this.descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDateTime getFechaEnvio() {
        return this.fechaEnvio;
    }

    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }
    
}
