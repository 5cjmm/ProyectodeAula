package com.ShopMaster.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.ShopMaster.Model.Proveedores;
import com.ShopMaster.Service.ProductosService;
import com.ShopMaster.Service.ProveedorService;

@Controller
@RequestMapping("/tendero")
public class TenderoController {

    private ProveedorService proveedorService;

    @GetMapping
    public String mostrarPuntodeVenta() {
        return "PuntoVenta";
    }

    public TenderoController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @GetMapping("/RegistroProveedor")
    public String mostrarProveedores(Model model) {
        model.addAttribute("proveedores", proveedorService.obtenerTodosLosProveedores());
        model.addAttribute("nuevoProveedor", new Proveedores());
        return "RegistroProveedor";
    }
   
}
