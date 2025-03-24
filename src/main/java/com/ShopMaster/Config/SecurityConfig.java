package com.ShopMaster.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import com.ShopMaster.Repository.UsuarioRepository;
import com.ShopMaster.Service.CustomUserDetailsService;

@Configuration
public class SecurityConfig {
   
        @Bean
        public UserDetailsService userDetailsService(UsuarioRepository
        usuarioRepository) {
        return new CustomUserDetailsService(usuarioRepository);
    }

        @Bean
        public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

        @Bean
        public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

        @Bean
        public AuthenticationManager authenticationManager(HttpSecurity http,
        UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) throws
        Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
        .authenticationProvider(authenticationProvider(userDetailsService, passwordEncoder))
        .build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
        .csrf(csrf -> csrf.disable()) // Desactivar CSRF solo para pruebas
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/login", "/home", "/favicon.ico").permitAll()
            .requestMatchers("/tendero/**").hasAuthority("ROLE_TENDERO")
            .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
            .anyRequest().authenticated()
        )
        .formLogin(form -> form
            .loginPage("/login")
            .successHandler((request, response, authentication) -> {
                authentication.getAuthorities().forEach(grantedAuthority -> {
                    try {
                        if (grantedAuthority.getAuthority().equals("ROLE_ADMIN")) {
                            response.sendRedirect("/admin");
                        } else if (grantedAuthority.getAuthority().equals("ROLE_TENDERO")) {
                            response.sendRedirect("/tendero");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            })
            .permitAll()
        )
        

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );
    
        return http.build();
    }
    
}
