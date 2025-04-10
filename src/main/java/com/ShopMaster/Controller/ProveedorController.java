package com.ShopMaster.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ShopMaster.Model.Proveedores;
import com.ShopMaster.Service.ProveedorService;

@Controller
@RequestMapping("/tendero")
public class ProveedorController {
    
    @Autowired
    private ProveedorService proveedorService;

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @PostMapping("/crear-proveedor")
    public String guardarProveedor(@ModelAttribute("nuevoProveedor") Proveedores proveedores, RedirectAttributes redirectAttributes) {
        proveedorService.guardarProveedor(proveedores);
        redirectAttributes.addFlashAttribute("SuccessMessage", "¡Proveedor guardado exitosamente!");
        return "redirect:/tendero/RegistroProveedor";
    }

    @PostMapping("/actualizar")
    public String actuaizarProveedor(@ModelAttribute Proveedores proveedor, RedirectAttributes redirectAttributes) {
        proveedorService.actualizarProveedor(proveedor);
        redirectAttributes.addFlashAttribute("SuccessMessage", "¡Producto actualizado exitosamente!");
        return "redirect:/tendero/RegistroProveedor";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarProveedor(@PathVariable String id, RedirectAttributes redirectAttributes) {
        proveedorService.eliminarProveedor(id);
        redirectAttributes.addFlashAttribute("SuccessMessage", "¡Producto eliminado exitosamente!");
        return "redirect:/tendero/RegistroProveedor";
    }

}
