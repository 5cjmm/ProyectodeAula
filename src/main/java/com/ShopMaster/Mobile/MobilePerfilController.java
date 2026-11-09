package com.ShopMaster.Mobile;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ShopMaster.Model.Usuario;

/**
 *  - GET  /api/perfil           datos del usuario y su tienda activa
 *  - POST /api/perfil/password  cambiar la contraseña con la sesión iniciada
 */
@RestController
@RequestMapping("/api/perfil")
public class MobilePerfilController {

    public record CambiarPasswordRequest(String passwordActual, String nuevaPassword) {
    }

    private final MobileContextService contexto;
    private final MobilePasswordService passwordService;

    public MobilePerfilController(MobileContextService contexto, MobilePasswordService passwordService) {
        this.contexto = contexto;
        this.passwordService = passwordService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> perfil() {
        Usuario u = contexto.usuarioActual();
        MobileContextService.TiendaActual tienda = contexto.tiendaDe(u);

        Map<String, Object> t = new LinkedHashMap<>();
        t.put("id", tienda.id());
        t.put("nombre", tienda.nombre());

        Map<String, Object> d = new LinkedHashMap<>();
        d.put("id", u.getId());
        d.put("nombre", u.getUsername());
        d.put("email", u.getEmail());
        d.put("rol", rol(u));
        d.put("tienda", t);
        return ResponseEntity.ok(MobileApiResponse.ok(d));
    }

    @PostMapping("/password")
    public ResponseEntity<Map<String, Object>> cambiarPassword(@RequestBody CambiarPasswordRequest req) {
        Usuario u = contexto.usuarioActual();
        String mensaje = passwordService.cambiar(u, req.passwordActual(), req.nuevaPassword());
        return ResponseEntity.ok(MobileApiResponse.mensaje(mensaje));
    }

    /** ADMIN o TENDERO (si tiene ambos roles gana ADMIN). */
    static String rol(Usuario u) {
        if (u.getRoles() != null && u.getRoles().contains("ROLE_ADMIN")) {
            return "ADMIN";
        }
        return "TENDERO";
    }
}
