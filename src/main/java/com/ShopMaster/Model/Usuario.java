package com.ShopMaster.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "usuarios") 
public class Usuario {
    
    @Id
    private String id;
    private String username;

    @Indexed(unique = true)
    private String email;
    
    private String password;
    private Set<String> roles;
    private List<Tienda> tiendas = new ArrayList<>();
    

    public Usuario() {}


    public Usuario(String id, String username, String email, String password, Set<String> roles, List<Tienda> tiendas) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.tiendas = tiendas;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }


    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }


    public List<Tienda> getTiendas() {
        return this.tiendas;
    }

    public void setTiendas(List<Tienda> tiendas) {
        this.tiendas = tiendas;
    }

    
}


    
   