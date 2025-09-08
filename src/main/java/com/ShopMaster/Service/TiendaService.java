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

    

    public Usuario agregarTiendaAlUsuario(String username, Tienda tienda) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Generar un ID único para la tienda
        tienda.setId(UUID.randomUUID().toString());

        usuario.getTiendas().add(tienda);
        return usuarioRepository.save(usuario);
    }

    public List<Tienda> obtenerTiendasDeUsuario(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return usuario.getTiendas();
    }
}
