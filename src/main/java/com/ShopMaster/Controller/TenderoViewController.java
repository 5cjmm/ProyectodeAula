/*package com.ShopMaster.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/tendero/tiendas")
public class TenderoViewController {
    @GetMapping("/{id}/inventario")
    public String verInventario(@PathVariable String id, Model model) {
        model.addAttribute("tiendaId", id); // se pasa a Inventario.html
        return "Inventario"; // templates/Inventario.html
    }

    @GetMapping("/{id}/proveedores")
    public String verProveedor(@PathVariable String id, Model model) {
        model.addAttribute("tiendaId", id); // se pasa a RegistroProveedor.html
        return "RegistroProveedor"; // templates/RegistroProveedor.html
    }

    @GetMapping("/{id}/deudas")
    public String verDeudas(@PathVariable String id, Model model) {
        model.addAttribute("tiendaId", id); // se pasa a Deudas.html
        return "Deudas"; // templates/Deudas.html
    }

    @GetMapping("/{id}/puntoventa")
    public String verPuntoVenta(@PathVariable String id, Model model) {
        model.addAttribute("tiendaId", id); // se pasa a PuntoVenta.html
        return "PuntoVenta"; // templates/PuntoVenta.html
    }
    
}
*/