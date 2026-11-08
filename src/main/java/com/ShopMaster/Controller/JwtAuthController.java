package com.ShopMaster.Controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ShopMaster.Mobile.MobileApiResponse;
import com.ShopMaster.Mobile.MobileAuthService;
import com.ShopMaster.Mobile.MobilePasswordService;
import com.ShopMaster.dto.AuthRequest;

/**
 * Acceso de la app móvil (rutas públicas, no llevan token):
 *  - POST /api/auth/login        correo + contraseña → token JWT
 *  - POST /api/auth/recuperar    envía un código de 6 dígitos al correo
 *  - POST /api/auth/restablecer  código + nueva contraseña
 */
@RestController
@RequestMapping("/api/auth")
public class JwtAuthController {

    public record RecuperarRequest(String email) {
    }

    public record RestablecerRequest(String email, String codigo, String nuevaPassword) {
    }

    private final MobileAuthService authService;
    private final MobilePasswordService passwordService;

    public JwtAuthController(MobileAuthService authService, MobilePasswordService passwordService) {
        this.authService = authService;
        this.passwordService = passwordService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody AuthRequest req) {
        return ResponseEntity.ok(MobileApiResponse.ok(authService.login(req.getEmail(), req.getPassword())));
    }

    @PostMapping("/recuperar")
    public ResponseEntity<Map<String, Object>> recuperar(@RequestBody RecuperarRequest req) {
        return ResponseEntity.ok(MobileApiResponse.mensaje(passwordService.recuperar(req.email())));
    }

    @PostMapping("/restablecer")
    public ResponseEntity<Map<String, Object>> restablecer(@RequestBody RestablecerRequest req) {
        String mensaje = passwordService.restablecer(req.email(), req.codigo(), req.nuevaPassword());
        return ResponseEntity.ok(MobileApiResponse.mensaje(mensaje));
    }
}
