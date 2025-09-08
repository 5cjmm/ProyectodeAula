package com.ShopMaster.Model;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "ventas")
public class Venta {
    @Id
    private String id;
    private Date fecha;
    private double total;
    private String tiendaId;
    private List<ProductoVendido> productos;
    

    public Venta() {
    }


    public Venta(String id, Date fecha, double total, String tiendaId, List<ProductoVendido> productos) {
        this.id = id;
        this.fecha = fecha;
        this.total = total;
        this.tiendaId = tiendaId;
        this.productos = productos;
    }



    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }
    

    public Date getFecha() {
        return this.fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public double getTotal() {
        return this.total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getTiendaId() {
        return this.tiendaId;
    }

    public void setTiendaId(String tiendaId) {
        this.tiendaId = tiendaId;
    }

    public List<ProductoVendido> getProductos() {
        return this.productos;
    }

    public void setProductos(List<ProductoVendido> productos) {
        this.productos = productos;
    }
    
}

