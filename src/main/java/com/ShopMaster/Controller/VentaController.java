package com.ShopMaster.Controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.ShopMaster.Model.Productos;
import com.ShopMaster.Model.Venta;
import com.ShopMaster.Repository.ProductosRepository;
import com.ShopMaster.Repository.VentaRepository;
import com.ShopMaster.dto.ProductoVendido;

@Controller
@RequestMapping("/tendero")
@SessionAttributes("productosSeleccionados")
public class VentaController {

    @Autowired
    private ProductosRepository productoRepo;

    @Autowired
    private VentaRepository ventaRepo;

    @ModelAttribute("productosSeleccionados")
    public List<ProductoVendido> productosSeleccionados() {
        return new ArrayList<>();
    }

    // Mostrar formulario principal
    @GetMapping("/crear")
    public String mostrarFormulario(Model model,
                                    @ModelAttribute("productosSeleccionados") List<ProductoVendido> seleccionados) {
        List<Productos> productos = productoRepo.findAll();
        model.addAttribute("todosProductos", productos);
        model.addAttribute("productosSeleccionados", seleccionados);
        model.addAttribute("totalVenta", seleccionados.stream().mapToDouble(p -> p.getCantidad() * p.getPrecioUnitario()).sum());
        return "PuntoVenta";
    }

    // Agregar producto al carrito
    @PostMapping("/agregar-producto")
    public String agregarProducto(@RequestParam("nombreProducto") String nombreProducto,
                                  @RequestParam("cantidad") int cantidad,
                                  @ModelAttribute("productosSeleccionados") List<ProductoVendido> seleccionados,
                                  Model model) {

        Productos producto = productoRepo.findByNombre(nombreProducto);

        if (producto == null) {
            model.addAttribute("error", "El Producto no existe.");
            return "PuntoVenta";
        }

        if (cantidad <= 0) {
            model.addAttribute("error", "La cantidad debe ser mayor a cero.");
            return "PuntoVenta";
        }

        if (cantidad > producto.getCantidad()) {
            model.addAttribute("error", "Stock insuficiente. Solo hay " + producto.getCantidad() + " disponibles.");
            return "PuntoVenta";
        }

        ProductoVendido vendido = new ProductoVendido();
        vendido.setProductoId(producto.getId());
        vendido.setCodigo(producto.getCodigo());
        vendido.setNombre(producto.getNombre());
        vendido.setCantidad(cantidad);
        vendido.setPrecioUnitario(producto.getPrecio());

        seleccionados.add(vendido);

        return "PuntoVenta";
    }

    // Eliminar producto del carrito
    @GetMapping("/eliminar/{codigo}")
    public String eliminarProducto(@PathVariable("codigo") String codigo,
                                   @ModelAttribute("productosSeleccionados") List<ProductoVendido> seleccionados) {
        seleccionados.removeIf(p -> p.getCodigo().equals(codigo));
        return "PuntoVenta";
    }

    // Guardar venta y actualizar inventario
    @PostMapping("/guardar")
    public String guardarVenta(@ModelAttribute("productosSeleccionados") List<ProductoVendido> seleccionados,
                               Model model) {

        if (seleccionados.isEmpty()) {
            model.addAttribute("error", "No hay productos en la venta.");
            return "PuntoVenta";
        }

        for (ProductoVendido p : seleccionados) {
            Productos prod = productoRepo.findById(p.getProductoId()).orElse(null);
            if (prod == null) continue;
            if (prod.getCantidad() < p.getCantidad()) {
                model.addAttribute("error", "Stock insuficiente para el producto: " + prod.getNombre());
                return "PuntoVenta";
            }

            // Restar stock
            prod.setCantidad(prod.getCantidad() - p.getCantidad());
            productoRepo.save(prod);
        }

        // Crear y guardar venta
        Venta venta = new Venta();
        venta.setFecha(new Date());
        venta.setTotal(seleccionados.stream().mapToDouble(p -> p.getCantidad() * p.getPrecioUnitario()).sum());
        venta.setProductos(new ArrayList<>(seleccionados));
        ventaRepo.save(venta);

        // Limpiar carrito
        seleccionados.clear();

        model.addAttribute("success", "Venta realizada con éxito.");
        return "PuntoVenta";
    }
}
