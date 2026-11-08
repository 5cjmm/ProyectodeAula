package com.ShopMaster.Security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

class JwtRequestFilterTest {

    private static final String SECRETO = "una-clave-de-prueba-de-mas-de-32-caracteres!!";

    private JwtUtil jwtUtil;
    private UserDetailsService usuarios;
    private JwtRequestFilter filtro;

    @BeforeEach
    void preparar() {
        SecurityContextHolder.clearContext();
        jwtUtil = new JwtUtil();
        jwtUtil.setSecret(SECRETO);
        usuarios = mock(UserDetailsService.class);
        filtro = new JwtRequestFilter();
        ReflectionTestUtils.setField(filtro, "jwtUtil", jwtUtil);
        ReflectionTestUtils.setField(filtro, "userDetailsService", usuarios);
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    private MockFilterChain ejecutar(String authorization) throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        if (authorization != null) {
            req.addHeader("Authorization", authorization);
        }
        MockFilterChain cadena = new MockFilterChain();
        filtro.doFilter(req, new MockHttpServletResponse(), cadena);
        return cadena;
    }

    @Test
    void tokenValidoAutenticaAlUsuario() throws Exception {
        when(usuarios.loadUserByUsername("ana@mitienda.com")).thenReturn(
                new User("ana@mitienda.com", "x", List.of(new SimpleGrantedAuthority("ROLE_TENDERO"))));
        String token = jwtUtil.generateToken("ana@mitienda.com");

        MockFilterChain cadena = ejecutar("Bearer " + token);

        assertNotNull(cadena.getRequest(), "la petición debe seguir su camino");
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("ana@mitienda.com", SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void tokenBasuraNoRompeLaPeticionNiAutentica() throws Exception {
        MockFilterChain cadena = ejecutar("Bearer esto.no.es.un.token");
        assertNotNull(cadena.getRequest());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void tokenFirmadoConOtraClaveNoAutentica() throws Exception {
        JwtUtil otro = new JwtUtil(); // clave aleatoria distinta
        MockFilterChain cadena = ejecutar("Bearer " + otro.generateToken("ana@mitienda.com"));
        assertNotNull(cadena.getRequest());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void tokenDeUnUsuarioQueYaNoExisteNoAutentica() throws Exception {
        when(usuarios.loadUserByUsername("fantasma@mitienda.com")).thenThrow(new UsernameNotFoundException("no existe"));
        MockFilterChain cadena = ejecutar("Bearer " + jwtUtil.generateToken("fantasma@mitienda.com"));
        assertNotNull(cadena.getRequest());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void sinEncabezadoLaPeticionContinuaSinAutenticar() throws Exception {
        MockFilterChain cadena = ejecutar(null);
        assertNotNull(cadena.getRequest());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    // ── JwtUtil: clave fija desde JWT_SECRET ──────────────────────────────────

    @Test
    void conUnSecretoFijoLosTokensSobrevivenAUnReinicio() {
        JwtUtil antes = new JwtUtil();
        antes.setSecret(SECRETO);
        JwtUtil despues = new JwtUtil(); // "servidor reiniciado"
        despues.setSecret(SECRETO);

        String token = antes.generateToken("ana@mitienda.com");
        assertEquals("ana@mitienda.com", despues.extractUsername(token));
        assertTrue(despues.validateToken(token, "ana@mitienda.com"));
        assertEquals(3600, antes.getExpirationSeconds());
    }

    @Test
    void unSecretoCortoSeRechazaAlArrancar() {
        JwtUtil util = new JwtUtil();
        assertThrows(IllegalStateException.class, () -> util.setSecret("muy-corto"));
    }

    @Test
    void sinSecretoSeSigueUsandoUnaClaveAleatoria() {
        JwtUtil util = new JwtUtil();
        util.setSecret("");
        assertEquals("a@b.co", util.extractUsername(util.generateToken("a@b.co")));
    }
}
