package com.ShopMaster.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ShopMaster.Service.ProveedorService;

@Controller
@RequestMapping("/tendero")
public class TenderoController {

    @SuppressWarnings({"FieldMayBeFinal", "unused"})
    private ProveedorService proveedorService;

   /*  @GetMapping("/PuntoVenta")
    public String mostrarPuntodeVenta(Model model) {
        return "PuntoVenta";
    }*/

    public TenderoController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @GetMapping("/Deudas")
    public String mostrarDeudas() {
        return "Deudas";
    }

   
}
