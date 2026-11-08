package com.ShopMaster.Mobile;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ShopMaster.Model.Tienda;
import com.ShopMaster.Model.Usuario;
import com.ShopMaster.Repository.UsuarioRepository;

/**
 * Resuelve quién hace la petición (usuario del token) y cuál es su tienda activa:
 * la primera tienda del usuario, igual que hace la web con el rol TENDERO.
 * La tienda nunca llega como parámetro desde la app.
 */
@Service
public class MobileContextService {

    public record TiendaActual(String id, String nombre) {
    }

    private final UsuarioRepository usuarioRepository;

    public MobileContextService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw MobileException.unauthorized("Token inválido o vencido");
        }
        Usuario usuario = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> MobileException.unauthorized("Token inválido o vencido"));
        if (!usuario.isActivo()) {
            throw MobileException.unauthorized("Token inválido o vencido");
        }
        return usuario;
    }

    public TiendaActual tiendaActual() {
        return tiendaDe(usuarioActual());
    }

    public TiendaActual tiendaDe(Usuario usuario) {
        List<Tienda> tiendas = usuario.getTiendas();
        if (tiendas == null || tiendas.isEmpty() || tiendas.get(0) == null || tiendas.get(0).getId() == null) {
            throw MobileException.forbidden("Tu usuario no tiene una tienda asignada.");
        }
        Tienda tienda = tiendas.get(0);
        String nombre = tienda.getNombre();
        if (nombre == null || nombre.isBlank()) {
            // A los tenderos solo se les guarda el id de la tienda; el nombre vive en el
            // usuario administrador que la creó.
            nombre = usuarioRepository.findByTiendasId(tienda.getId()).stream()
                    .filter(u -> u.getTiendas() != null)
                    .flatMap(u -> u.getTiendas().stream())
                    .filter(t -> t != null && tienda.getId().equals(t.getId())
                            && t.getNombre() != null && !t.getNombre().isBlank())
                    .map(Tienda::getNombre)
                    .findFirst()
                    .orElse("Mi tienda");
        }
        return new TiendaActual(tienda.getId(), nombre);
    }
}
