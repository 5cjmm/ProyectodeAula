package com.ShopMaster.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.ShopMaster.Repository.UsuarioRepository;
import com.ShopMaster.Security.CustomAuthenticationSuccessHandler;
import com.ShopMaster.Security.JwtRequestFilter;
import com.ShopMaster.Service.CustomUserDetailsService;

@Configuration
public class SecurityConfig {

    @Autowired
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Bean
    public UserDetailsService userDetailsService(UsuarioRepository usuarioRepository) {
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
                                                       UserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .authenticationProvider(authenticationProvider(userDetailsService, passwordEncoder))
                .build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtRequestFilter jwtRequestFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                // Recursos estáticos y páginas públicas
                        .requestMatchers("/login", "/home", "/", "/favicon.ico", "/api/auth/**").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**",
                    "/fonts/**", "/register").permitAll() // AÑADIDO

                        .requestMatchers("/login", "/register", "/success", "/home", "/favicon.ico", "/api/auth/**", "/api/pqrs").permitAll()
                        .requestMatchers("/tiendas", "/tiendas/RegistroTendero", "/tiendas/{id}/tendero").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/tenderos/**", "/api/usuarios/**").hasAuthority("ROLE_ADMIN")

                        // 🟩 Rutas compartidas entre ADMIN y TENDERO
                        .requestMatchers("/tiendas/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_TENDERO")
                        .requestMatchers("/api/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_TENDERO")
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email") // <--- importante: campo del formulario
                        .passwordParameter("password") // opcional, por claridad
                        .successHandler(customAuthenticationSuccessHandler)
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