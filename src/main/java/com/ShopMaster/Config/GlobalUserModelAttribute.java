package com.ShopMaster.Config;

import com.ShopMaster.Model.Usuario;
import com.ShopMaster.Repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalUserModelAttribute {

    private final UsuarioRepository usuarioRepository;

    public GlobalUserModelAttribute(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @ModelAttribute("usuario")
    public Usuario addUsuarioToModel() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return usuarioRepository.findByUsername(auth.getName()).orElse(null);
        }
        return null;
    }
}
