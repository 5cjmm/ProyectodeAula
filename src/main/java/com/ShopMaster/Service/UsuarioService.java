package com.ShopMaster.Service;

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

    public boolean existePorUsername(String username) {
        return usuarioRepository.findByUsername(username) != null;
    }

    public void actualizarUsuario(Usuario usuario) {
        if (usuarioRepository.existsById(usuario.getId())) {
            usuarioRepository.save(usuario);
        }
    }
}

