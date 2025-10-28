package com.ShopMaster.Security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.ShopMaster.Model.Usuario;
import com.ShopMaster.Repository.UsuarioRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public CustomAuthenticationSuccessHandler(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        String username = authentication.getName();
        String token = jwtUtil.generateToken(username);

        System.out.println("Autenticación exitosa: " + username);
        System.out.println("Token JWT generado: " + token);

        Cookie jwtCookie = new Cookie("jwt", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(60 * 60); // 1 hora
        response.addCookie(jwtCookie);

        HttpSession session = request.getSession();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String role = authentication.getAuthorities().iterator().next().getAuthority();
        session.setAttribute("usuarioLogueado", usuario);
        session.setAttribute("rolUsuario", role);

        if (role.equals("ROLE_ADMIN")) {
            response.sendRedirect("/tiendas");
            return;
        } 
        else if (role.equals("ROLE_TENDERO")) {
            if (usuario.getTiendas() != null && !usuario.getTiendas().isEmpty()) {
                String tiendaId = usuario.getTiendas().get(0).getId();
                response.sendRedirect("/tiendas/" + tiendaId + "/dashboard");
            } else {
                response.sendRedirect("/sin-tienda");
            }
            return;
        }
        
        response.sendRedirect("/home");
    }
}
