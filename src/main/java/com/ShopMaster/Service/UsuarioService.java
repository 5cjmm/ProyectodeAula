package com.ShopMaster.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ShopMaster.Model.Usuario;
import com.ShopMaster.Repository.UsuarioRepository;

@Service
public class UsuarioService {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void guardarUsuario(Usuario usuario) {
        String encriptada = passwordEncoder.encode(usuario.getPassword());
        usuario.setPassword(encriptada);

        usuarioRepository.save(usuario);
    }

    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }

    public boolean existePorUsername(String username) {
        return usuarioRepository.findByUsername(username) != null;
    }

   public void actualizarUsuario(Usuario usuario) {
    Optional<Usuario> existenteOpt = usuarioRepository.findById(usuario.getId());

    if (existenteOpt.isPresent()) {
        Usuario existente = existenteOpt.get();
        existente.setUsername(usuario.getUsername());

        if (!usuario.getPassword().isBlank()) {
            String encriptada = passwordEncoder.encode(usuario.getPassword());
            existente.setPassword(encriptada);
        }

        existente.setRoles(usuario.getRoles());

        // Aquí puedes añadir más campos si los hay...

        usuarioRepository.save(existente);
    }
}


    public void eliminarUsuario(String id) {
        usuarioRepository.deleteById(id);
    }
}

