package com.ShopMaster.Model;

import java.time.LocalDateTime;

public class Abono {
    private double monto;
    private LocalDateTime fecha = LocalDateTime.now();

    public Abono() {}


    public Abono(double monto, LocalDateTime fecha) {
        this.monto = monto;
        this.fecha = fecha;
    }


    public double getMonto() {
        return this.monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public LocalDateTime getFecha() {
        return this.fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
    
}