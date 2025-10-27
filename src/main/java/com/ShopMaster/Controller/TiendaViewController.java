package com.ShopMaster.Controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ShopMaster.Model.Usuario;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/tiendas")
public class TiendaViewController {

    @GetMapping
    public String showTiendasPage(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        model.addAttribute("usuario", usuario); // se pasa a tiendas.html
        return "tiendas"; // busca templates/tiendas.html
    }

    @GetMapping("/{id}/dashboard")
    public String verDashboard(@PathVariable String id, Model model) {
        model.addAttribute("tiendaId", id);
        return "Dashboard";
    }

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
    public String verPuntoVenta(@PathVariable String id, Authentication authentication, Model model) {
        model.addAttribute("tiendaId", id);
        model.addAttribute("rolUsuario", authentication.getAuthorities().iterator().next().getAuthority());
        return "PuntoVenta"; // templates/PuntoVenta.html
    }

    @GetMapping("/{id}/informe")
    public String verInforme(@PathVariable String id, Model model) {
        model.addAttribute("tiendaId", id); // se pasa a Informe.html
        return "InformeVentas"; // templates/Informe.html
    }

    @GetMapping("/{id}/tendero")
    public String verTendero(@PathVariable String id, Model model) {
        model.addAttribute("tiendaId", id); // se pasa a Tendero.html
        return "RegistroTendero"; // templates/Tendero.html

    }

    @GetMapping("/{id}/perfil")
    public String verPerfil(@PathVariable String id, Model model) {
        model.addAttribute("tiendaId", id);
        return "Perfil";
    }
}