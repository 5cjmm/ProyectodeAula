package com.ShopMaster.Model;

import org.bson.types.ObjectId;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductoVendido {

    private String productoId;      // ID del producto en la colección "productos"
    private String codigo;          // Código del producto
    private String nombre;          // Nombre del producto
    private int cantidad;           // Cantidad vendida
    private double precioUnitario;  // Precio de venta por unidad
    private double subtotal;        // cantidad * precioUnitario

    // Constructor auxiliar para crear fácilmente desde un objeto Productos
    public static ProductoVendido desdeProducto(ObjectId id, String codigo, String nombre, int cantidad, double precio) {
        ProductoVendido pv = new ProductoVendido();
        pv.setProductoId(id.toHexString());
        pv.setCodigo(codigo);
        pv.setNombre(nombre);
        pv.setCantidad(cantidad);
        pv.setPrecioUnitario(precio);
        pv.setSubtotal(precio * cantidad);
        return pv;
    }

    // Compatibilidad: algunos controladores llaman a getPrecio()
    public double getPrecio() {
        return this.precioUnitario;
    }
}
