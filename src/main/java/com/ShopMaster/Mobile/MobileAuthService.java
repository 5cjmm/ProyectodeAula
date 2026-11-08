package com.ShopMaster.Mobile;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import com.ShopMaster.Model.Usuario;
import com.ShopMaster.Repository.UsuarioRepository;
import com.ShopMaster.Security.JwtUtil;

/** POST /api/auth/login: valida las credenciales con Spring Security y entrega el token JWT. */
@Service
public class MobileAuthService {

    private static final String CREDENCIALES = "Correo o contraseña incorrectos";

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;

    public MobileAuthService(AuthenticationManager authenticationManager,
                             JwtUtil jwtUtil,
                             UsuarioRepository usuarioRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.usuarioRepository = usuarioRepository;
    }

    public Map<String, Object> login(String email, String password) {
        String correo = email == null ? "" : email.trim();
        if (correo.isEmpty() || password == null || password.isEmpty()) {
            throw MobileException.unauthorized(CREDENCIALES);
        }
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(correo, password));
        } catch (AuthenticationException e) {
            throw MobileException.unauthorized(CREDENCIALES);
        }
        // Un usuario eliminado lógicamente no puede entrar aunque su contraseña sea correcta.
        Usuario usuario = usuarioRepository.findByEmail(correo)
                .filter(Usuario::isActivo)
                .orElseThrow(() -> MobileException.unauthorized(CREDENCIALES));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token", jwtUtil.generateToken(usuario.getEmail()));
        data.put("tipo", "Bearer");
        data.put("expiraEnSegundos", jwtUtil.getExpirationSeconds());
        return data;
    }
}
