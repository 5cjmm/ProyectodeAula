package com.ShopMaster.Controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ShopMaster.Model.Tienda;
import com.ShopMaster.Model.Usuario;
import com.ShopMaster.Service.TiendaService;

@RestController
@RequestMapping("/api/tiendas")
public class TiendaRestController {

    @Autowired
    private TiendaService tiendaService;

    // Obtener tiendas del usuario logueado
    @GetMapping
    public List<Tienda> listarTiendas(Principal principal) {
        String email = principal.getName();
    return tiendaService.obtenerTiendasDeUsuario(email);
    }

    // Crear tienda
    @PostMapping
    public Usuario crearTienda(@RequestBody Tienda tienda, Principal principal) {
        String email = principal.getName();
        return tiendaService.agregarTiendaAlUsuario(email, tienda);
    }
}
