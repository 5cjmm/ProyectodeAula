package com.ShopMaster;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
           BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
           String rawPassword = "1234567";
           String encodedPassword = encoder.encode(rawPassword);
           System.out.println("Contraseña encriptada: " + encodedPassword);
       }
   }