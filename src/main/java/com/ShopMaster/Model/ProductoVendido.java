package com.ShopMaster.Model;

import org.bson.types.ObjectId;

public class ProductoVendido {

    private String productoId;
    private String codigo;
    private String nombre;
    private int cantidad;
    private double precioUnitario;
    private double subtotal;

    public ProductoVendido() {}

    public ProductoVendido(String productoId, String codigo, String nombre,
                           int cantidad, double precioUnitario, double subtotal) {
        this.productoId     = productoId;
        this.codigo         = codigo;
        this.nombre         = nombre;
        this.cantidad       = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal       = subtotal;
    }

    // Constructor auxiliar desde un objeto Productos
    public static ProductoVendido desdeProducto(ObjectId id, String codigo,
                                                String nombre, int cantidad, double precio) {
        ProductoVendido pv = new ProductoVendido();
        pv.setProductoId(id.toHexString());
        pv.setCodigo(codigo);
        pv.setNombre(nombre);
        pv.setCantidad(cantidad);
        pv.setPrecioUnitario(precio);
        pv.setSubtotal(precio * cantidad);
        return pv;
    }

    // Getters
    public String getProductoId()      { return productoId; }
    public String getCodigo()          { return codigo; }
    public String getNombre()          { return nombre; }
    public int    getCantidad()        { return cantidad; }
    public double getPrecioUnitario()  { return precioUnitario; }
    public double getSubtotal()        { return subtotal; }

    // Compatibilidad: algunos controladores llaman a getPrecio()
    public double getPrecio()          { return precioUnitario; }

    // Setters
    public void setProductoId(String productoId)         { this.productoId     = productoId; }
    public void setCodigo(String codigo)                 { this.codigo         = codigo; }
    public void setNombre(String nombre)                 { this.nombre         = nombre; }
    public void setCantidad(int cantidad)                { this.cantidad       = cantidad; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }
    public void setSubtotal(double subtotal)             { this.subtotal       = subtotal; }
}
