package com.ShopMaster.Mobile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.ShopMaster.Model.Tienda;
import com.ShopMaster.Model.Usuario;
import com.ShopMaster.Repository.UsuarioRepository;
import com.ShopMaster.Service.ChatBotService;

class MobileControllersTest {

    private UsuarioRepository usuarioRepository;
    private MobileContextService contexto;

    @BeforeEach
    void preparar() {
        usuarioRepository = mock(UsuarioRepository.class);
        contexto = new MobileContextService(usuarioRepository);
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    private static void iniciarSesionComo(String email) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null, List.of()));
    }

    private static Usuario usuario(String email, String rol, Tienda... tiendas) {
        Usuario u = new Usuario();
        u.setId("U1");
        u.setUsername("Ana Martínez");
        u.setEmail(email);
        u.setRoles(Set.of(rol));
        u.setActivo(true);
        u.setTiendas(new ArrayList<>(List.of(tiendas)));
        return u;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> data(ResponseEntity<Map<String, Object>> r) {
        return (Map<String, Object>) r.getBody().get("data");
    }

    // ── Contexto: usuario y tienda del token ──────────────────────────────────

    @Test
    void sinSesionResponde401() {
        MobileException e = assertThrows(MobileException.class, () -> contexto.usuarioActual());
        assertEquals(HttpStatus.UNAUTHORIZED, e.getStatus());
    }

    @Test
    void usuarioEliminadoLogicamenteResponde401() {
        Usuario u = usuario("ana@mitienda.com", "ROLE_TENDERO", new Tienda("T1"));
        u.setActivo(false);
        when(usuarioRepository.findByEmail("ana@mitienda.com")).thenReturn(Optional.of(u));
        iniciarSesionComo("ana@mitienda.com");
        assertEquals(HttpStatus.UNAUTHORIZED,
                assertThrows(MobileException.class, () -> contexto.usuarioActual()).getStatus());
    }

    @Test
    void usuarioSinTiendaResponde403() {
        Usuario u = usuario("ana@mitienda.com", "ROLE_ADMIN");
        when(usuarioRepository.findByEmail("ana@mitienda.com")).thenReturn(Optional.of(u));
        iniciarSesionComo("ana@mitienda.com");
        MobileException e = assertThrows(MobileException.class, () -> contexto.tiendaActual());
        assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
        assertEquals("Tu usuario no tiene una tienda asignada.", e.getMessage());
    }

    @Test
    void elTenderoTomaElNombreDeLaTiendaDelAdministrador() {
        Usuario tendero = usuario("tendero@mitienda.com", "ROLE_TENDERO", new Tienda("T1")); // solo trae el id
        Usuario admin = usuario("admin@mitienda.com", "ROLE_ADMIN",
                new Tienda("T0", "Otra tienda", "Calle 1", "Abarrotes", "111"),
                new Tienda("T1", "Mi tienda del barrio", "Calle 2", "Abarrotes", "222"));
        when(usuarioRepository.findByEmail("tendero@mitienda.com")).thenReturn(Optional.of(tendero));
        when(usuarioRepository.findByTiendasId("T1")).thenReturn(List.of(tendero, admin));
        iniciarSesionComo("tendero@mitienda.com");

        MobileContextService.TiendaActual t = contexto.tiendaActual();
        assertEquals("T1", t.id());
        assertEquals("Mi tienda del barrio", t.nombre());
    }

    // ── GET /api/perfil ───────────────────────────────────────────────────────

    @Test
    void perfilDevuelveUsuarioRolYTienda() {
        Usuario admin = usuario("ana@mitienda.com", "ROLE_ADMIN",
                new Tienda("T1", "Mi tienda", "Calle 2", "Abarrotes", "222"));
        when(usuarioRepository.findByEmail("ana@mitienda.com")).thenReturn(Optional.of(admin));
        iniciarSesionComo("ana@mitienda.com");

        MobilePerfilController controlador = new MobilePerfilController(contexto, mock(MobilePasswordService.class));
        Map<String, Object> d = data(controlador.perfil());
        assertEquals("U1", d.get("id"));
        assertEquals("Ana Martínez", d.get("nombre"));
        assertEquals("ana@mitienda.com", d.get("email"));
        assertEquals("ADMIN", d.get("rol"));
        @SuppressWarnings("unchecked")
        Map<String, Object> tienda = (Map<String, Object>) d.get("tienda");
        assertEquals("T1", tienda.get("id"));
        assertEquals("Mi tienda", tienda.get("nombre"));
    }

    // ── POST /api/chat ────────────────────────────────────────────────────────

    @Test
    void chatUsaLaTiendaDelTokenYNoLaDelCliente() {
        Usuario u = usuario("ana@mitienda.com", "ROLE_TENDERO",
                new Tienda("T1", "Mi tienda", "Calle 2", "Abarrotes", "222"));
        when(usuarioRepository.findByEmail("ana@mitienda.com")).thenReturn(Optional.of(u));
        iniciarSesionComo("ana@mitienda.com");
        ChatBotService bot = mock(ChatBotService.class);
        when(bot.responder("¿Cuánto vendí hoy?", "T1")).thenReturn("Hoy vendiste $120.000");

        MobileChatController controlador = new MobileChatController(contexto, bot);
        Map<String, Object> d = data(controlador.chat(new MobileChatController.ChatRequest("  ¿Cuánto vendí hoy?  ")));
        assertEquals("Hoy vendiste $120.000", d.get("respuesta"));
    }

    @Test
    void chatRechazaMensajesVaciosOMuyLargos() {
        ChatBotService bot = mock(ChatBotService.class);
        MobileChatController controlador = new MobileChatController(contexto, bot);

        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(MobileException.class,
                () -> controlador.chat(new MobileChatController.ChatRequest("   "))).getStatus());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(MobileException.class,
                () -> controlador.chat(new MobileChatController.ChatRequest(null))).getStatus());
        String largo = "a".repeat(MobileChatController.MAX_CARACTERES + 1);
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(MobileException.class,
                () -> controlador.chat(new MobileChatController.ChatRequest(largo))).getStatus());
        verify(bot, never()).responder(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }

    // ── Formato de los errores ────────────────────────────────────────────────

    @Test
    void elManejadorDeErroresUsaElFormatoDelContrato() {
        MobileExceptionHandler manejador = new MobileExceptionHandler();

        ResponseEntity<Map<String, Object>> r = manejador.negocio(MobileException.notFound("Producto no encontrado"));
        assertEquals(HttpStatus.NOT_FOUND, r.getStatusCode());
        assertEquals(false, r.getBody().get("success"));
        assertEquals("Producto no encontrado", r.getBody().get("message"));

        ResponseEntity<Map<String, Object>> inesperado = manejador.inesperado(new RuntimeException("detalle interno"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, inesperado.getStatusCode());
        assertEquals("Error interno del servidor", inesperado.getBody().get("message")); // no filtra el detalle
    }
}
