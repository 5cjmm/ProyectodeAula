package com.ShopMaster.Model;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Código de un solo uso para recuperar la contraseña desde la app móvil.
 * Se guarda cifrado (BCrypt), vence a los 15 minutos y permite pocos intentos.
 */
@Document(collection = "password_resets")
public class PasswordReset {

    @Id
    private String id;

    @Indexed
    private String email;

    private String codigoHash;

    // Mongo borra el documento solo cuando llega esta fecha (si el índice TTL está creado)
    @Indexed(expireAfter = "0s")
    private Date expiraEn;

    private int intentos;

    private Date creadoEn;

    public PasswordReset() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCodigoHash() { return codigoHash; }
    public void setCodigoHash(String codigoHash) { this.codigoHash = codigoHash; }

    public Date getExpiraEn() { return expiraEn; }
    public void setExpiraEn(Date expiraEn) { this.expiraEn = expiraEn; }

    public int getIntentos() { return intentos; }
    public void setIntentos(int intentos) { this.intentos = intentos; }

    public Date getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Date creadoEn) { this.creadoEn = creadoEn; }
}
