package com.ShopMaster.Controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ShopMaster.Model.Productos;
import com.ShopMaster.Repository.ProductosRepository;
import com.ShopMaster.Service.ProductosService;

@Controller
@RequestMapping("/admin")
public class ProductoController {
    
    private ProductosService productosService;

    @Autowired
    private ProductosRepository productosRepository;

    public ProductoController(ProductosService productosService) {
        this.productosService = productosService;
    }

    @PostMapping("/crear-producto")
    public String guardarProducto(@ModelAttribute("nuevoProducto") Productos productos) {
        productosService.guardarProducto(productos);
        return "redirect:/admin";
    }


    @PostMapping("/actualizar")
    public String actualizarProductos(@ModelAttribute Productos productos) {
        productosService.actualizarProductos(productos);
        return "redirect:/admin";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable String id) {
        productosService.eliminarProducto(id);
        return "redirect:/admin";
    }

    @GetMapping("/{codigo}")
    public Optional<Productos> obtenerProducto(@PathVariable int codigo) {
        return productosRepository.findByCodigo(codigo);
    }

}
