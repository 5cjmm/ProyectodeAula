package com.ShopMaster.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ShopMaster.Model.Proveedores;
import com.ShopMaster.Service.ProductosService;
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
    public String guardarProveedor(@ModelAttribute("nuevoProveedor") Proveedores proveedores) {
        proveedorService.guardarProveedor(proveedores);
        return "redirect:/tendero/RegistroProveedor";
    }

    @PostMapping("/actualizar")
    public String actuaizarProveedor(@ModelAttribute Proveedores proveedor) {
        proveedorService.actualizarProveedor(proveedor);
        return "RegistroProveedor";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarProveedor(@PathVariable String id) {
        proveedorService.eliminarProveedor(id);
        return "RegistroProveedor";
    }

}
