package com.ShopMaster.Controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ShopMaster.Model.Usuario;
import com.ShopMaster.Service.UsuarioService;

@RestController
@RequestMapping("/api/tenderos")
@PreAuthorize("hasRole('ADMIN')")
public class TenderoRestController {

    private final UsuarioService usuarioService;

    public TenderoRestController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/crear")
    public Usuario crearTendero(@RequestBody Usuario tendero, @RequestParam String tiendaId) {
        return usuarioService.registrarTendero(tendero, tiendaId);
    }

    @PutMapping("/actualizar/{id}")
    public Usuario actualizarTendero(@PathVariable String id, @RequestBody Usuario tendero) {
        return usuarioService.actualizarTendero(id, tendero);
    }

    @DeleteMapping("/eliminar/{id}")
    public void eliminarTendero(@PathVariable String id) {
        usuarioService.eliminarTendero(id);
    }

    @GetMapping("/tienda/{tiendaId}")
    public List<Usuario> listarTenderos(@PathVariable String tiendaId) {
        return usuarioService.listarTenderosPorTienda(tiendaId);
    }
}

