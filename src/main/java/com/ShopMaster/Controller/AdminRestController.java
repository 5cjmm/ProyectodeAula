package com.ShopMaster.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ShopMaster.Model.Usuario;
import com.ShopMaster.Service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
public class AdminRestController {

    @Autowired
    private UsuarioService usuarioService;

    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Usuario> actualizarPerfilAdmin(@PathVariable String id, @RequestBody Usuario datosActualizados) {
        Usuario actualizado = usuarioService.actualizarAdmin(id, datosActualizados);
        return ResponseEntity.ok(actualizado);
    }

    
}
