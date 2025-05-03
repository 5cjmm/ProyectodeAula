package com.ShopMaster.Controller;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ShopMaster.Model.Productos;
import com.ShopMaster.Repository.ProductoRepositoryCustom;
import com.ShopMaster.Repository.ProductosRepository;
import com.ShopMaster.Service.ProductosService;
import com.ShopMaster.Service.ProveedorService;

@Controller
@RequestMapping("/admin")
public class ProductoController {
    
    @Autowired
    @SuppressWarnings("FieldMayBeFinal")
    private ProductosService productosService;

    @Autowired
    private ProductosRepository productosRepository;

    @Autowired
    ProveedorService proveedorService;

    @Autowired
    private ProductoRepositoryCustom productoRepositoryCustom;

    public ProductoController(ProductosService productosService) {
        this.productosService = productosService;
    }

    @PostMapping("/crear-producto")
    public String guardarProducto(@ModelAttribute Productos producto,
                                  @RequestParam("proveedorIdStrs") List<String> proveedorIdStrs,
                                  RedirectAttributes redirectAttributes) {

        List<ObjectId> proveedorIds = proveedorIdStrs.stream()
                .map(ObjectId::new)
                .toList();

        producto.setProveedorIds(proveedorIds);
        productosService.guardarProducto(producto);
        redirectAttributes.addFlashAttribute("SuccessMessage", "Producto registrado exitosamente");

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
