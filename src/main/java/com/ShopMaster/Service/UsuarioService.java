package com.ShopMaster.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ShopMaster.Model.Tienda;
import com.ShopMaster.Model.Usuario;
import com.ShopMaster.Repository.UsuarioRepository;

@Service
public class UsuarioService {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void guardarUsuario(Usuario usuario) { 
            if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("Ya existe un usuario con ese email");
        }

        String encriptada = passwordEncoder.encode(usuario.getPassword());
        usuario.setPassword(encriptada);

        usuarioRepository.save(usuario);
    }

    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }

    /*public boolean existePorUsername(String username) {
        return usuarioRepository.findByUsername(username) != null;
    }*/


    public Usuario actualizarAdmin(String id, Usuario datosActualizados) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Actualizar username y email
        if (datosActualizados.getUsername() != null && !datosActualizados.getUsername().isBlank()) {
            usuario.setUsername(datosActualizados.getUsername());
        }

        if (datosActualizados.getEmail() != null && !datosActualizados.getEmail().isBlank()) {
            usuario.setEmail(datosActualizados.getEmail());
        }

        if (datosActualizados.getPassword() != null && !datosActualizados.getPassword().isBlank()) {
            String nuevaPassword = passwordEncoder.encode(datosActualizados.getPassword());
            usuario.setPassword(nuevaPassword);
        }

        return usuarioRepository.save(usuario);
    }
    

    // 🔹 Registrar tendero
    public Usuario registrarTendero(Usuario tendero, String tiendaId) {
    
        if (usuarioRepository.existsByEmail(tendero.getEmail())) {
            throw new RuntimeException("El correo ya está en uso");
        }

        // Asignar rol y encriptar contraseña
        tendero.setPassword(passwordEncoder.encode(tendero.getPassword()));
        tendero.setRoles(Set.of("ROLE_TENDERO"));

        // Asignar tienda
        if (tendero.getTiendas() == null) {
            tendero.setTiendas(new ArrayList<>());
        }
        tendero.getTiendas().add(new Tienda(tiendaId)); // o un objeto tienda completo

        return usuarioRepository.save(tendero);
    }

    // 🔹 Actualizar tendero
    public Usuario actualizarTendero(String id, Usuario tendero) {
        Usuario existente = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tendero no encontrado"));

        existente.setEmail(tendero.getEmail());
        existente.setUsername(tendero.getUsername());
        return usuarioRepository.save(existente);
    }

    // 🔹 Eliminar tendero
    public void eliminarTendero(String id) {
        usuarioRepository.deleteById(id);
    }

    // 🔹 Listar tenderos de una tienda
    public List<Usuario> listarTenderosPorTienda(String tiendaId) {
        return usuarioRepository.findByRolesContainingAndTiendasId("ROLE_TENDERO", tiendaId);
    }
}

