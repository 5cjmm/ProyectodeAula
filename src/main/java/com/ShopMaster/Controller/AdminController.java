package com.ShopMaster.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ShopMaster.Model.Productos;
import com.ShopMaster.Service.ProductosService;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final ProductosService productosService;

    public AdminController(ProductosService productosService) {
        this.productosService = productosService;
    }

    @GetMapping
    public String mostrarInventario(Model model) {
        model.addAttribute("productos", productosService.obtenerTodosLosProductos());
        model.addAttribute("nuevoProducto", new Productos());
        return "Inventario";
    }

    @PostMapping("/crear-producto")
    public String guardarProducto(@ModelAttribute("nuevoProducto") Productos productos) {
        productosService.guardarProducto(productos);
        return "redirect:/Inventario";
    }


    @PostMapping("/actualizar")
    public String actualizarProductos(@ModelAttribute Productos productos) {
        productosService.actualizarProductos(productos);
        return "redirect:/admin";
    }

    
}