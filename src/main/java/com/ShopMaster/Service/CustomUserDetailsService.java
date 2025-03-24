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

@Override
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

 System.out.println("Intentando autenticar usuario: " + username);

 Usuario usuario = usuarioRepository.findByUsername(username)

    .orElseThrow(() -> {
        System.out.println("Usuario no encontrado en la base de datos.");
        return new UsernameNotFoundException("Usuario no encontrado");
    });
   
    System.out.println("Usuario encontrado: " + usuario.getUsername());
        return new User(
            usuario.getUsername(),
            usuario.getPassword(),   
            usuario.getRoles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList())
        );
    }
}