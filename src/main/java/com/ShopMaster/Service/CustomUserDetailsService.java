package com.ShopMaster.Service;

import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ShopMaster.Model.Usuario;
import com.ShopMaster.Repository.UsuarioRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // Spring Security llamará a este método con el "username" recibido del formulario.
    // En nuestro caso será el email porque configuraremos usernameParameter("email").
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        System.out.println("Intentando autenticar (email): " + email);

        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> {
                System.out.println("Usuario (email) no encontrado en la base de datos: " + email);
                return new UsernameNotFoundException("Usuario no encontrado");
            });

        System.out.println("Usuario encontrado: " + usuario.getEmail());
        return new User(
            usuario.getEmail(),                    // username interno = email
            usuario.getPassword(),
            usuario.getRoles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList())
        );
    }
}
