package com.ShopMaster.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ShopMaster.Model.Proveedor;
import com.ShopMaster.Service.ProveedorService;

@Controller
@RequestMapping("/tendero")
public class TenderoController {

    @SuppressWarnings("FieldMayBeFinal")
    private ProveedorService proveedorService;

    @GetMapping("/PuntoVenta")
    public String mostrarPuntodeVenta(Model model) {
        return "PuntoVenta";
    }

    public TenderoController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    

    @GetMapping("/RegistroProveedor")
    public String mostrarProveedores(Model model) {
        model.addAttribute("proveedores", proveedorService.obtenerTodosLosProveedores());
        model.addAttribute("nuevoProveedor", new Proveedor());
        return "RegistroProveedor";
    }
   
}
