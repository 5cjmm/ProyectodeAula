package com.ShopMaster.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.ShopMaster.Model.Productos;
import com.ShopMaster.Model.Venta;
import com.ShopMaster.Repository.ProductosRepository;
import com.ShopMaster.Repository.VentaRepository;

@Controller
@RequestMapping("/tendero")
@SessionAttributes("carrito")
public class VentaController {

    @Autowired
    private ProductosRepository productosRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @ModelAttribute("carrito")
    public List<Venta> carrito() {
        return new ArrayList<>();
    }

    @PostMapping("/agregar")
    public String agregarProducto(@RequestParam("codigo") int codigo, 
                                  @RequestParam("cantidad") int cantidad, 
                                  @ModelAttribute("carrito") List<Venta> carrito, 
                                  Model model) {
        Optional<Productos> productoOpt = productosRepository.findByCodigo(codigo);

        if (!productoOpt.isPresent()) {
            model.addAttribute("error", "El producto no existe.");
            return "PuntoVenta"; // Vuelve a la vista
        }

        Productos producto = productoOpt.get();

        if (cantidad > producto.getCantidad()) {
            model.addAttribute("error", "Stock insuficiente. Solo hay " + producto.getCantidad() + " unidades disponibles.");
            return "PuntoVenta";
        }

        double total = cantidad * producto.getPrecio();

        // Crear objeto Venta para agregarlo al carrito
        Venta venta = new Venta(cantidad, null, null, cantidad, total, total);
        venta.setCodigo(producto.getCodigo());
        venta.setNombre(producto.getNombre());
        venta.setCantidad(cantidad);
        venta.setPrecio(producto.getPrecio());
        venta.setTotal(total);

        // Agregar el producto al carrito
        carrito.add(venta);

        // Enviar los datos a la vista
        model.addAttribute("carrito", carrito);
        model.addAttribute("totalVenta", carrito.stream().mapToDouble(Venta::getTotal).sum());

        return "PuntoVenta";
    }

        @GetMapping("/eliminar/{codigo}")
        public String eliminarProducto(@PathVariable("codigo") int codigo, 
                                    @ModelAttribute("carrito") List<Venta> carrito, 
                                    Model model) {
        carrito.removeIf(p -> p.getCodigo() == codigo);
        model.addAttribute("totalVenta", carrito.stream().mapToDouble(Venta::getTotal).sum());
        return "PuntoVenta";
    }

    @PostMapping("/finalizar")
public String finalizarVenta(@ModelAttribute("carrito") List<Venta> carrito, Model model) {
    if (carrito.isEmpty()) {
        model.addAttribute("error", "No hay productos en la venta.");
        return "PuntoVenta";
    }

    // Guardar ventas en la base de datos
    ventaRepository.saveAll(carrito);

    // Limpiar el carrito después de la compra
    carrito.clear();
    
    model.addAttribute("carrito", carrito);
    model.addAttribute("totalVenta", 0.0);
    model.addAttribute("success", "Venta realizada con exito.");
    return "PuntoVenta";
}


}