package com.ShopMaster.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.ShopMaster.Model.Usuario;

@Repository
public interface UsuarioRepository extends MongoRepository<Usuario, String> {
    // ── Consultas con Soft Delete ──
    Optional<Usuario> findByEmailAndActivoTrue(String email);
    Optional<Usuario> findByUsernameAndActivoTrue(String username);
    boolean           existsByEmailAndActivoTrue(String email);

    // ── Métodos legacy ──
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByUsername(String username);
    boolean           existsByEmail(String email);

    List<Usuario> findByRolesContainingAndTiendasId(String rol, String tiendaId);
}
