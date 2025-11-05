package com.ShopMaster.Service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ShopMaster.Model.Tienda;
import com.ShopMaster.Model.Usuario;
import com.ShopMaster.Repository.UsuarioRepository;

@Service
public class TiendaService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    public Usuario agregarTiendaAlUsuario(String email, Tienda tienda) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        boolean existeNit = usuario.getTiendas().stream()
                .anyMatch(t -> t.getNit().equalsIgnoreCase(tienda.getNit()));

        if (existeNit) {
            throw new RuntimeException("Ya existe una tienda con ese NIT en este usuario");
        }

        tienda.setId(UUID.randomUUID().toString());
        usuario.getTiendas().add(tienda);

        return usuarioRepository.save(usuario);
    }

    public List<Tienda> obtenerTiendasDeUsuario(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return usuario.getTiendas();
    }
}
