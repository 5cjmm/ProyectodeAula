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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ShopMaster.Model.ProductoVendido;
import com.ShopMaster.Model.Productos;
import com.ShopMaster.Model.Venta;
import com.ShopMaster.Repository.ProductosRepository;
import com.ShopMaster.Repository.VentaRepository;

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
    @GetMapping("/PuntoVenta")
    public String mostrarFormulario(Model model,
                                    @ModelAttribute("productosSeleccionados") List<ProductoVendido> seleccionados) {
        List<Productos> productos = productoRepo.findByCantidadGreaterThan(0);
        model.addAttribute("todosProductos", productos);
        model.addAttribute("productosSeleccionados", seleccionados);
        model.addAttribute("totalVenta", seleccionados.stream().mapToDouble(p -> p.getCantidad() * p.getPrecio()).sum());
        return "PuntoVenta";
    }

    // Agregar producto al carrito
    @PostMapping("/agregar-producto")
public String agregarProducto(@RequestParam("nombreProducto") String nombreProducto,
                              @RequestParam("cantidad") int cantidad,
                              @ModelAttribute("productosSeleccionados") List<ProductoVendido> seleccionados,
                              Model model, RedirectAttributes redirectAttributes) {

    Productos producto = productoRepo.findByNombre(nombreProducto);

    if (producto == null) {
        redirectAttributes.addFlashAttribute("error", "El Producto no existe.");
        return "redirect:/tendero/PuntoVenta";
    }

    if (cantidad <= 0) {
        redirectAttributes.addFlashAttribute("error", "La cantidad debe ser mayor a cero.");
        return "redirect:/tendero/PuntoVenta";
    }

    // Buscar si ya está en la lista
    ProductoVendido existente = seleccionados.stream()
        .filter(p -> p.getProductoId().equals(producto.getId()))
        .findFirst()
        .orElse(null);

    if (existente != null) {
        int nuevaCantidad = existente.getCantidad() + cantidad;

        if (nuevaCantidad > producto.getCantidad()) {
            redirectAttributes.addFlashAttribute("error", "Stock insuficiente. Solo hay " + producto.getCantidad() + " disponibles.");
            return "redirect:/tendero/PuntoVenta";
        }

        // Sumar cantidades si ya existe
        existente.setCantidad(nuevaCantidad);

        redirectAttributes.addFlashAttribute("success", "Cantidad actualizada para el producto: " + producto.getNombre());
    } else {
        if (cantidad > producto.getCantidad()) {
            redirectAttributes.addFlashAttribute("error", "Stock insuficiente. Solo hay " + producto.getCantidad() + " disponibles.");
            return "redirect:/tendero/PuntoVenta";
        }

        // Si no existe, lo agregas normalmente
        ProductoVendido vendido = new ProductoVendido();
        vendido.setProductoId(producto.getId());
        vendido.setCodigo(producto.getCodigo());
        vendido.setNombre(producto.getNombre());
        vendido.setCantidad(cantidad);
        vendido.setPrecio(producto.getPrecio());

        seleccionados.add(vendido);

       
    }

    return "redirect:/tendero/PuntoVenta";
}

    // Eliminar producto del carrito
    @GetMapping("/eliminar/{codigo}")
    public String eliminarProducto(@PathVariable("codigo") String codigo,
                                   @ModelAttribute("productosSeleccionados") List<ProductoVendido> seleccionados) {
        seleccionados.removeIf(p -> p.getCodigo().equals(codigo));
        return "redirect:/tendero/PuntoVenta";
    }

    // Guardar venta y actualizar inventario
    @PostMapping("/guardar")
    public String guardarVenta(@ModelAttribute("productosSeleccionados") List<ProductoVendido> seleccionados,
                               Model model, RedirectAttributes redirectAttributes) {

        if (seleccionados.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No hay productos en la venta.");
            return "redirect:/tendero/PuntoVenta";
        }

        for (ProductoVendido p : seleccionados) {
            Productos prod = productoRepo.findById(p.getProductoId()).orElse(null);
            if (prod == null) continue;
            if (prod.getCantidad() < p.getCantidad()) {
                model.addAttribute("error", "Stock insuficiente para el producto: " + prod.getNombre());
                return "redirect:/tendero/PuntoVenta";
            }

            // Restar stock
            prod.setCantidad(prod.getCantidad() - p.getCantidad());
            productoRepo.save(prod);
        }

        // Crear y guardar venta
        Venta venta = new Venta();
        venta.setFecha(new Date());
        venta.setTotal(seleccionados.stream().mapToDouble(p -> p.getCantidad() * p.getPrecio()).sum());
        venta.setProductos(new ArrayList<>(seleccionados));
        ventaRepo.save(venta);

        // Limpiar carrito
        seleccionados.clear();

        redirectAttributes.addFlashAttribute("success", "Venta realizada con éxito.");
        return "redirect:/tendero/PuntoVenta";
    }
}
