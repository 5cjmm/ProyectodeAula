package com.ShopMaster.Model;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;


@Document(collection = "ventas")
public class Venta {

    @Id
    private String id;
    private int codigo;
    private String cliente;
    private String nombre;
    private int cantidad;
    private double precio;
    private double total;
    private Date fecha;

    public Venta(Productos producto, int cantidad2) {}

    public Venta(int codigo, String cliente, String nombre, int cantidad, double precio, double total) {
        this.codigo = codigo;
        this.cliente = cliente;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.precio = precio;
        this.total = total;
        this.fecha = new Date();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getCodigo() { return codigo; }
    public void setCodigo(int codigo) { this.codigo = codigo; }

    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }

    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

}


