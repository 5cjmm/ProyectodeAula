package com.ShopMaster.Model;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.ShopMaster.dto.ProductoVendido;


@Document(collection = "ventas")
public class Venta {
    @Id
    private ObjectId id;
    private Date fecha;
    private double total;
    private List<ProductoVendido> productos;
    

    public ObjectId getId() {
        return this.id;
    }

    public void setId(ObjectId id) {
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

    public List<ProductoVendido> getProductos() {
        return this.productos;
    }

    public void setProductos(List<ProductoVendido> productos) {
        this.productos = productos;
    }
    
}

