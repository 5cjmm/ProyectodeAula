package com.ShopMaster.Mobile;

import java.security.SecureRandom;
import java.time.Clock;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ShopMaster.Model.PasswordReset;
import com.ShopMaster.Model.Usuario;
import com.ShopMaster.Repository.PasswordResetRepository;
import com.ShopMaster.Repository.UsuarioRepository;

/**
 * Recuperar y cambiar la contraseña desde la app móvil.
 *
 * Flujo de recuperación: la app pide un código con el correo, el servidor envía un
 * código de 6 dígitos (vence a los 15 minutos) y la app lo envía junto con la nueva
 * contraseña. El código se guarda cifrado y se bloquea tras 5 intentos fallidos.
 */
@Service
public class MobilePasswordService {

    public static final String MENSAJE_RECUPERAR =
            "Si el correo está registrado, recibirás un código de 6 dígitos para crear una nueva contraseña.";
    public static final String MENSAJE_RESTABLECIDA = "Tu contraseña fue actualizada. Ya puedes ingresar.";
    public static final String MENSAJE_CAMBIADA = "Tu contraseña fue actualizada.";

    static final int MINUTOS_VIGENCIA = 15;
    static final int MAX_INTENTOS = 5;
    static final int SEGUNDOS_ENTRE_ENVIOS = 60;

    private static final Logger log = LoggerFactory.getLogger(MobilePasswordService.class);
    private static final Pattern CORREO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final String CODIGO_INVALIDO = "El código no es válido o venció";
    private static final String POLITICA =
            "La contraseña debe tener entre 8 y 72 caracteres, con al menos una letra y un número.";

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetRepository resetRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectProvider<JavaMailSender> mailSender;
    private final String remitente;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    // El envío del correo se hace en segundo plano para que la respuesta tarde lo mismo
    // exista o no el correo (así no se revela qué correos están registrados).
    Executor ejecutor = CompletableFuture::runAsync;

    @Autowired
    public MobilePasswordService(UsuarioRepository usuarioRepository,
                                 PasswordResetRepository resetRepository,
                                 PasswordEncoder passwordEncoder,
                                 ObjectProvider<JavaMailSender> mailSender,
                                 @Value("${spring.mail.username:}") String remitente) {
        this(usuarioRepository, resetRepository, passwordEncoder, mailSender, remitente, Clock.systemUTC());
    }

    MobilePasswordService(UsuarioRepository usuarioRepository,
                          PasswordResetRepository resetRepository,
                          PasswordEncoder passwordEncoder,
                          ObjectProvider<JavaMailSender> mailSender,
                          String remitente,
                          Clock clock) {
        this.usuarioRepository = usuarioRepository;
        this.resetRepository = resetRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.remitente = remitente;
        this.clock = clock;
    }

    // ── POST /api/auth/recuperar ──────────────────────────────────────────────

    public String recuperar(String email) {
        String correo = limpiar(email);
        if (!correoValido(correo)) {
            throw MobileException.badRequest("El correo no tiene un formato válido");
        }

        Optional<Usuario> usuario = buscarUsuarioActivo(correo);
        if (usuario.isPresent()) {
            String emailGuardado = usuario.get().getEmail();
            Optional<PasswordReset> previo = resetRepository.findByEmail(emailGuardado);
            long ahora = clock.millis();
            boolean muyReciente = previo.isPresent() && previo.get().getCreadoEn() != null
                    && ahora - previo.get().getCreadoEn().getTime() < SEGUNDOS_ENTRE_ENVIOS * 1000L;

            if (!muyReciente) {
                String codigo = String.format("%06d", random.nextInt(1_000_000));
                resetRepository.deleteByEmail(emailGuardado);

                PasswordReset reset = new PasswordReset();
                reset.setEmail(emailGuardado);
                reset.setCodigoHash(passwordEncoder.encode(codigo));
                reset.setCreadoEn(new Date(ahora));
                reset.setExpiraEn(new Date(ahora + MINUTOS_VIGENCIA * 60_000L));
                reset.setIntentos(0);
                resetRepository.save(reset);

                ejecutor.execute(() -> enviarCorreo(emailGuardado, codigo));
            }
        }
        // Misma respuesta exista o no el correo.
        return MENSAJE_RECUPERAR;
    }

    private void enviarCorreo(String destino, String codigo) {
        try {
            JavaMailSender sender = mailSender.getIfAvailable();
            if (sender == null) {
                log.warn("No hay correo configurado (spring.mail.*); no se pudo enviar el código de recuperación.");
                return;
            }
            SimpleMailMessage mensaje = new SimpleMailMessage();
            if (remitente != null && !remitente.isBlank()) {
                mensaje.setFrom(remitente);
            }
            mensaje.setTo(destino);
            mensaje.setSubject("ShopMaster · Código para recuperar tu contraseña");
            mensaje.setText("Hola,\n\n"
                    + "Tu código para crear una nueva contraseña en ShopMaster es:\n\n"
                    + "    " + codigo + "\n\n"
                    + "Vence en " + MINUTOS_VIGENCIA + " minutos. Si no lo pediste tú, ignora este mensaje: "
                    + "tu contraseña actual sigue funcionando.\n\n"
                    + "Equipo ShopMaster");
            sender.send(mensaje);
        } catch (Exception e) {
            log.error("No se pudo enviar el código de recuperación", e);
        }
    }

    // ── POST /api/auth/restablecer ────────────────────────────────────────────

    public String restablecer(String email, String codigo, String nuevaPassword) {
        String correo = limpiar(email);
        if (!correoValido(correo)) {
            throw MobileException.badRequest("El correo no tiene un formato válido");
        }
        String cod = limpiar(codigo);
        if (!cod.matches("\\d{6}")) {
            throw MobileException.badRequest("El código debe tener 6 dígitos");
        }
        validarPassword(nuevaPassword);

        Usuario usuario = buscarUsuarioActivo(correo)
                .orElseThrow(() -> MobileException.badRequest(CODIGO_INVALIDO));
        PasswordReset reset = resetRepository.findByEmail(usuario.getEmail())
                .orElseThrow(() -> MobileException.badRequest(CODIGO_INVALIDO));

        boolean vencido = reset.getExpiraEn() == null || reset.getExpiraEn().getTime() <= clock.millis();
        if (vencido || reset.getIntentos() >= MAX_INTENTOS) {
            resetRepository.deleteByEmail(usuario.getEmail());
            throw MobileException.badRequest(CODIGO_INVALIDO);
        }
        if (!passwordEncoder.matches(cod, reset.getCodigoHash())) {
            reset.setIntentos(reset.getIntentos() + 1);
            resetRepository.save(reset);
            throw MobileException.badRequest(CODIGO_INVALIDO);
        }

        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);
        resetRepository.deleteByEmail(usuario.getEmail());
        return MENSAJE_RESTABLECIDA;
    }

    // ── POST /api/perfil/password (con sesión iniciada) ───────────────────────

    public String cambiar(Usuario usuario, String passwordActual, String nuevaPassword) {
        if (passwordActual == null || passwordActual.isEmpty()) {
            throw MobileException.badRequest("Escribe tu contraseña actual");
        }
        validarPassword(nuevaPassword);
        // Se responde 400 (no 401) para que la app no confunda este error con una sesión vencida.
        if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            throw MobileException.badRequest("La contraseña actual no es correcta");
        }
        if (passwordActual.equals(nuevaPassword)) {
            throw MobileException.badRequest("La nueva contraseña debe ser diferente a la actual");
        }
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);
        return MENSAJE_CAMBIADA;
    }

    // ── Validaciones ──────────────────────────────────────────────────────────

    static void validarPassword(String password) {
        boolean ok = password != null
                && password.length() >= 8
                && password.length() <= 72
                && password.chars().anyMatch(Character::isLetter)
                && password.chars().anyMatch(Character::isDigit);
        if (!ok) {
            throw MobileException.badRequest(POLITICA);
        }
    }

    static boolean correoValido(String correo) {
        return correo != null && correo.length() <= 254 && CORREO.matcher(correo).matches();
    }

    private static String limpiar(String s) {
        return s == null ? "" : s.trim();
    }

    private Optional<Usuario> buscarUsuarioActivo(String correo) {
        Optional<Usuario> u = usuarioRepository.findByEmail(correo);
        if (u.isEmpty() && !correo.equals(correo.toLowerCase())) {
            u = usuarioRepository.findByEmail(correo.toLowerCase());
        }
        return u.filter(Usuario::isActivo);
    }
}
