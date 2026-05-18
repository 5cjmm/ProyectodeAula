package com.ShopMaster.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


import com.ShopMaster.Model.Usuario;
import com.ShopMaster.Repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void guardarUsuario_registroCorrecto_yEncriptaContrasena() {
        Usuario u = new Usuario();
        u.setEmail("test@example.com");
        u.setPassword("plainPass");

        when(usuarioRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plainPass")).thenReturn("ENCODED");
        when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        usuarioService.guardarUsuario(u);

        verify(passwordEncoder).encode("plainPass");
        verify(usuarioRepository).save(any(Usuario.class));
        assertEquals("ENCODED", u.getPassword());
    }

    @Test
    void guardarUsuario_emailRepetido_lanzaExcepcion() {
        Usuario u = new Usuario();
        u.setEmail("dup@example.com");
        u.setPassword("x");

        when(usuarioRepository.existsByEmail("dup@example.com")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> usuarioService.guardarUsuario(u));
        assertTrue(ex.getMessage().toLowerCase().contains("ya existe") || ex.getMessage().toLowerCase().contains("correo"));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void registrarTendero_asignaRolYtienda_yEncripta() {
        Usuario tendero = new Usuario();
        tendero.setEmail("tendero@example.com");
        tendero.setPassword("tpass");

        when(usuarioRepository.existsByEmail("tendero@example.com")).thenReturn(false);
        when(passwordEncoder.encode("tpass")).thenReturn("ENC_T");
        when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Usuario saved = usuarioService.registrarTendero(tendero, "tienda-123");

        verify(passwordEncoder).encode("tpass");
        verify(usuarioRepository).save(any(Usuario.class));
        assertNotNull(saved.getTiendas());
        assertFalse(saved.getTiendas().isEmpty());
        assertEquals("ENC_T", saved.getPassword());
        assertTrue(saved.getRoles().contains("ROLE_TENDERO"));
        // la tienda añadida contiene el id esperado
        assertEquals("tienda-123", saved.getTiendas().get(0).getId());
    }
}
