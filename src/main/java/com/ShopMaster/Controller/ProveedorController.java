package com.ShopMaster.Controller;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ShopMaster.Model.Proveedor;
import com.ShopMaster.Service.ProveedorService;

@Controller
@RequestMapping("/admin")
public class ProveedorController {
    
    @Autowired
    @SuppressWarnings("FieldMayBeFinal")
    private ProveedorService proveedorService;

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @PostMapping("/crear-proveedor")
    public String guardarProveedor(@ModelAttribute("nuevoProveedor") Proveedor proveedor, RedirectAttributes redirectAttributes) {
        proveedorService.guardarProveedor(proveedor);
        redirectAttributes.addFlashAttribute("SuccessMessage", "¡Proveedor guardado exitosamente!");
        return "redirect:/admin/RegistroProveedor";
    }

      @PostMapping("/actualizar-proveedor")
    public String actuaizarProveedor(@ModelAttribute Proveedor proveedor, RedirectAttributes redirectAttributes) {
        proveedorService.actualizarProveedor(proveedor);
        redirectAttributes.addFlashAttribute("SuccessMessage", "¡Proveedor actualizado exitosamente!");
        return "redirect:/admin/RegistroProveedor";
    }

    @PostMapping("/eliminar-proveedor/{id}")
    public String eliminarProveedor(@PathVariable ObjectId id, RedirectAttributes redirectAttributes) {
        proveedorService.eliminarProveedor(id);
        redirectAttributes.addFlashAttribute("SuccessMessage", "¡Proveedor eliminado exitosamente!");
        return "redirect:/admin/RegistroProveedor";
    }

}
