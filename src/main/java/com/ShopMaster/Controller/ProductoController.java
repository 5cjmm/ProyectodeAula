package com.ShopMaster.Controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String guardarProducto(@ModelAttribute("nuevoProducto") Productos productos, RedirectAttributes redirectAttributes) {
        productosService.guardarProducto(productos);
        redirectAttributes.addFlashAttribute("SuccessMessage", "¡Producto guardado exitosamente!");
        return "redirect:/admin/Inventario";
    }

    @PostMapping("/actualizar")
    public String actualizarProductos(@ModelAttribute Productos productos, RedirectAttributes redirectAttributes) {
        productosService.actualizarProductos(productos);
        redirectAttributes.addFlashAttribute("SuccessMessage", "¡Producto actualizado exitosamente!");
        return "redirect:/admin/Inventario";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable String id, RedirectAttributes redirectAttributes) {
        productosService.eliminarProducto(id);
        redirectAttributes.addFlashAttribute("SuccessMessage", "¡Producto eliminado exitosamente!");
        return "redirect:/admin/Inventario";
    }

    @GetMapping("/{codigo}")
    public Optional<Productos> obtenerProducto(@PathVariable String codigo) {
        return productosRepository.findByCodigo(codigo);
    }

    

}
