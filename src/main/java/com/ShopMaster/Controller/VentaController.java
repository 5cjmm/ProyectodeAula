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
    public String agregarProducto(@RequestParam("codigo") String codigo, 
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

     
        Venta venta = new Venta();
        venta.setCodigo(producto.getCodigo());
        venta.setNombre(producto.getNombre());
        venta.setCliente(venta.getCliente());
        venta.setCantidad(cantidad);
        venta.setPrecio(producto.getPrecio());
        venta.setTotal(total);

        carrito.add(venta);

        
        model.addAttribute("carrito", carrito);
        model.addAttribute("totalVenta", carrito.stream().mapToDouble(Venta::getTotal).sum());

        return "PuntoVenta";
    }

        @GetMapping("/eliminar/{codigo}")
        public String eliminarProducto(@PathVariable("codigo") String codigo, 
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

        for (Venta venta : carrito) {
            Optional<Productos> productoOpt = productosRepository.findByCodigo(venta.getCodigo());
            if (productoOpt.isPresent()) {
                Productos producto = productoOpt.get();
                producto.setCantidad(producto.getCantidad() - venta.getCantidad());
                productosRepository.save(producto);
            }
        
    }
        


    ventaRepository.saveAll(carrito);

    carrito.clear();
    
    model.addAttribute("carrito", carrito);
    model.addAttribute("totalVenta", 0.0);
    model.addAttribute("success", "Venta realizada con exito.");
    return "PuntoVenta";
}

    
}
