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
        // ✅ Solo verifica emails de usuarios activos
        if (usuarioRepository.existsByEmailAndActivoTrue(usuario.getEmail())) {
            throw new RuntimeException("Ya existe un usuario con ese email");
        }
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setActivo(true);
        usuarioRepository.save(usuario);
    }

    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }

    public Usuario actualizarAdmin(String id, Usuario datosActualizados) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (datosActualizados.getUsername() != null && !datosActualizados.getUsername().isBlank()) {
            String nuevoUsername = datosActualizados.getUsername().trim();
            // ✅ Buscar solo entre usuarios activos
            Optional<Usuario> existente = usuarioRepository.findByUsernameAndActivoTrue(nuevoUsername);
            if (existente.isPresent() && !existente.get().getId().equals(id)) {
                throw new RuntimeException("El nombre de usuario ya está en uso");
            }
            usuario.setUsername(nuevoUsername);
        }

        if (datosActualizados.getEmail() != null && !datosActualizados.getEmail().isBlank()) {
            usuario.setEmail(datosActualizados.getEmail().trim());
        }

        if (datosActualizados.getPassword() != null && !datosActualizados.getPassword().isBlank()) {
            String password = datosActualizados.getPassword();
            boolean valida = password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$");
            if (!valida) {
                throw new RuntimeException("La contraseña debe tener al menos 8 caracteres, " +
                        "una mayúscula, una minúscula, un número y un símbolo.");
            }
            usuario.setPassword(passwordEncoder.encode(password));
        }

        return usuarioRepository.save(usuario);
    }

    // ── Tenderos ─────────────────────────────────────────────────

    public Usuario registrarTendero(Usuario tendero, String tiendaId) {
        // ✅ Solo verifica emails activos
        if (usuarioRepository.existsByEmailAndActivoTrue(tendero.getEmail())) {
            throw new RuntimeException("El correo ya está en uso");
        }

        tendero.setPassword(passwordEncoder.encode(tendero.getPassword()));
        tendero.setRoles(Set.of("ROLE_TENDERO"));
        tendero.setActivo(true);

        if (tendero.getTiendas() == null) {
            tendero.setTiendas(new ArrayList<>());
        }
        tendero.getTiendas().add(new Tienda(tiendaId));

        return usuarioRepository.save(tendero);
    }

    public Usuario actualizarTendero(String id, Usuario tendero) {
        Usuario existente = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tendero no encontrado"));

        existente.setEmail(tendero.getEmail());
        existente.setUsername(tendero.getUsername());
        return usuarioRepository.save(existente);
    }

    // ✅ Soft Delete — no borra de la BD
    public void eliminarTendero(String id) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tendero no encontrado: " + id));
        u.setActivo(false);
        usuarioRepository.save(u);
    }

    public List<Usuario> listarTenderosPorTienda(String tiendaId) {
        return usuarioRepository.findByRolesContainingAndTiendasId("ROLE_TENDERO", tiendaId);
    }
}
