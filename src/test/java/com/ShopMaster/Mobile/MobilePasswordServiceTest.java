package com.ShopMaster.Mobile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ShopMaster.Model.PasswordReset;
import com.ShopMaster.Model.Usuario;
import com.ShopMaster.Repository.PasswordResetRepository;
import com.ShopMaster.Repository.UsuarioRepository;

class MobilePasswordServiceTest {

    /** Reloj que se puede adelantar para probar la vigencia del código. */
    static class RelojMutable extends Clock {
        private Instant ahora = Instant.parse("2026-11-07T15:00:00Z");

        void avanzarSegundos(long s) {
            ahora = ahora.plusSeconds(s);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return ahora;
        }
    }

    private UsuarioRepository usuarioRepository;
    private PasswordResetRepository resetRepository;
    private PasswordEncoder encoder;
    private JavaMailSender correo;
    private RelojMutable reloj;
    private MobilePasswordService servicio;
    private Usuario ana;
    private final Map<String, PasswordReset> almacen = new HashMap<>();

    @SuppressWarnings("unchecked")
    @BeforeEach
    void preparar() {
        usuarioRepository = mock(UsuarioRepository.class);
        resetRepository = mock(PasswordResetRepository.class);
        encoder = new BCryptPasswordEncoder(4);
        correo = mock(JavaMailSender.class);
        reloj = new RelojMutable();

        ObjectProvider<JavaMailSender> proveedor = mock(ObjectProvider.class);
        when(proveedor.getIfAvailable()).thenReturn(correo);

        ana = new Usuario();
        ana.setId("U1");
        ana.setUsername("Ana Martínez");
        ana.setEmail("ana@mitienda.com");
        ana.setPassword(encoder.encode("Clave1234"));
        ana.setActivo(true);
        when(usuarioRepository.findByEmail("ana@mitienda.com")).thenReturn(Optional.of(ana));
        when(usuarioRepository.findByEmail("nadie@mitienda.com")).thenReturn(Optional.empty());

        when(resetRepository.findByEmail(any())).thenAnswer(inv -> Optional.ofNullable(almacen.get((String) inv.getArgument(0))));
        when(resetRepository.save(any())).thenAnswer(inv -> {
            PasswordReset r = inv.getArgument(0);
            almacen.put(r.getEmail(), r);
            return r;
        });
        doAnswer(inv -> {
            almacen.remove((String) inv.getArgument(0));
            return null;
        }).when(resetRepository).deleteByEmail(any());

        servicio = new MobilePasswordService(usuarioRepository, resetRepository, encoder, proveedor,
                "soporte@shopmaster.test", reloj);
        servicio.ejecutor = Runnable::run; // el correo se envía en el mismo hilo
    }

    /** Pide el código y lo lee del correo que se habría enviado. */
    private String pedirCodigo() {
        servicio.recuperar("ana@mitienda.com");
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(correo, times(1)).send(captor.capture());
        Matcher m = Pattern.compile("\\b(\\d{6})\\b").matcher(captor.getValue().getText());
        assertTrue(m.find(), "el correo debe traer un código de 6 dígitos");
        return m.group(1);
    }

    // ── recuperar ─────────────────────────────────────────────────────────────

    @Test
    void recuperarRechazaCorreosConFormatoInvalido() {
        MobileException e = assertThrows(MobileException.class, () -> servicio.recuperar("no-es-correo"));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("El correo no tiene un formato válido", e.getMessage());
    }

    @Test
    void recuperarResponde200ConElMismoMensajeAunqueElCorreoNoExista() {
        String mensaje = servicio.recuperar("nadie@mitienda.com");
        assertEquals(MobilePasswordService.MENSAJE_RECUPERAR, mensaje);
        verify(correo, never()).send(any(SimpleMailMessage.class));
        assertTrue(almacen.isEmpty());
    }

    @Test
    void recuperarGuardaElCodigoCifradoYEnviaElCorreo() {
        String codigo = pedirCodigo();
        PasswordReset reset = almacen.get("ana@mitienda.com");
        assertNotNull(reset);
        assertFalse(reset.getCodigoHash().contains(codigo), "el código no se guarda en texto plano");
        assertTrue(encoder.matches(codigo, reset.getCodigoHash()));
        assertEquals(0, reset.getIntentos());
        assertEquals(15 * 60_000L, reset.getExpiraEn().getTime() - reset.getCreadoEn().getTime());
    }

    @Test
    void recuperarNoReenviaCodigoAntesDeUnMinuto() {
        servicio.recuperar("ana@mitienda.com");
        servicio.recuperar("ana@mitienda.com");
        verify(correo, times(1)).send(any(SimpleMailMessage.class));

        reloj.avanzarSegundos(61);
        servicio.recuperar("ana@mitienda.com");
        verify(correo, times(2)).send(any(SimpleMailMessage.class));
    }

    @Test
    void usuarioInactivoNoRecibeCodigo() {
        ana.setActivo(false);
        servicio.recuperar("ana@mitienda.com");
        verify(correo, never()).send(any(SimpleMailMessage.class));
    }

    // ── restablecer ───────────────────────────────────────────────────────────

    @Test
    void restablecerConElCodigoCorrectoCambiaLaContrasena() {
        String codigo = pedirCodigo();
        String mensaje = servicio.restablecer("ana@mitienda.com", codigo, "NuevaClave99");

        assertEquals(MobilePasswordService.MENSAJE_RESTABLECIDA, mensaje);
        assertTrue(encoder.matches("NuevaClave99", ana.getPassword()));
        verify(usuarioRepository).save(ana);
        assertTrue(almacen.isEmpty(), "el código se usa una sola vez");

        // el mismo código ya no sirve
        assertThrows(MobileException.class, () -> servicio.restablecer("ana@mitienda.com", codigo, "OtraClave77"));
    }

    @Test
    void restablecerConCodigoIncorrectoSumaIntentosYSeBloqueaTrasCinco() {
        String codigo = pedirCodigo();
        String malo = codigo.equals("000000") ? "111111" : "000000";

        for (int i = 0; i < MobilePasswordService.MAX_INTENTOS; i++) {
            MobileException e = assertThrows(MobileException.class,
                    () -> servicio.restablecer("ana@mitienda.com", malo, "NuevaClave99"));
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("El código no es válido o venció", e.getMessage());
        }
        // ahora ni el código correcto sirve: hay que pedir uno nuevo
        assertThrows(MobileException.class, () -> servicio.restablecer("ana@mitienda.com", codigo, "NuevaClave99"));
        assertFalse(encoder.matches("NuevaClave99", ana.getPassword()));
    }

    @Test
    void restablecerConCodigoVencidoFalla() {
        String codigo = pedirCodigo();
        reloj.avanzarSegundos(16 * 60);
        MobileException e = assertThrows(MobileException.class,
                () -> servicio.restablecer("ana@mitienda.com", codigo, "NuevaClave99"));
        assertEquals("El código no es válido o venció", e.getMessage());
        assertFalse(encoder.matches("NuevaClave99", ana.getPassword()));
    }

    @Test
    void restablecerValidaLosDatosDeEntrada() {
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(MobileException.class,
                () -> servicio.restablecer("ana@mitienda.com", "12345", "NuevaClave99")).getStatus());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(MobileException.class,
                () -> servicio.restablecer("ana@mitienda.com", "123456", "corta1")).getStatus());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(MobileException.class,
                () -> servicio.restablecer("ana", "123456", "NuevaClave99")).getStatus());
    }

    // ── cambiar (con sesión) ──────────────────────────────────────────────────

    @Test
    void cambiarConLaClaveActualCorrectaActualizaLaContrasena() {
        String mensaje = servicio.cambiar(ana, "Clave1234", "OtraClave55");
        assertEquals(MobilePasswordService.MENSAJE_CAMBIADA, mensaje);
        assertTrue(encoder.matches("OtraClave55", ana.getPassword()));
        verify(usuarioRepository).save(ana);
    }

    @Test
    void cambiarFallaConClaveActualIncorrectaSinDevolver401() {
        MobileException e = assertThrows(MobileException.class, () -> servicio.cambiar(ana, "equivocada1", "OtraClave55"));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus()); // 400: la app no debe cerrar la sesión
        assertEquals("La contraseña actual no es correcta", e.getMessage());
    }

    @Test
    void cambiarExigeUnaClaveNuevaValidaYDistinta() {
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(MobileException.class,
                () -> servicio.cambiar(ana, "Clave1234", "corta1")).getStatus());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(MobileException.class,
                () -> servicio.cambiar(ana, "Clave1234", "sololetrasaqui")).getStatus());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(MobileException.class,
                () -> servicio.cambiar(ana, "Clave1234", "Clave1234")).getStatus());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(MobileException.class,
                () -> servicio.cambiar(ana, "", "OtraClave55")).getStatus());
    }
}
