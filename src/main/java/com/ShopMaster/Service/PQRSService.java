package com.ShopMaster.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.ShopMaster.Model.PQRS;
import com.ShopMaster.Repository.PQRSRepository;

@Service
public class PQRSService {

    @Autowired
    private PQRSRepository pqrsRepository;

    @Autowired
    private JavaMailSender mailSender;

    private static final String DESTINO = "soporte.shopmaster@gmail.com"; // 📧 Correo receptor fijo

    public PQRS enviarPQRS(PQRS pqrs) {
        pqrsRepository.save(pqrs);

        // Enviar correo
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(DESTINO);
        mensaje.setSubject("[PQRS] " + pqrs.getTipo() + " - " + pqrs.getAsunto());
        mensaje.setText(
                "📩 Nuevo PQRS recibido:\n\n" +
                "👤 Nombre: " + pqrs.getNombre() + "\n" +
                "✉️ Correo: " + pqrs.getEmail() + "\n" +
                "📂 Tipo: " + pqrs.getTipo() + "\n" +
                "📝 Asunto: " + pqrs.getAsunto() + "\n\n" +
                "Descripción:\n" + pqrs.getDescripcion()
        );

        mailSender.send(mensaje);
        return pqrs;
    }
}
