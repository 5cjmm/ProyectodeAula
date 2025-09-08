package com.ShopMaster.Controller;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ShopMaster.Model.Usuario;
import com.ShopMaster.Service.UsuarioService;

@Controller
public class AuthController {
 
    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("login") 
        public String loginPage(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Usuario o contraseña incorrectos.");
        }
        return "login";
    }

    @GetMapping("/register")
    public String mostrarFormulario(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "register"; // Vista Thymeleaf
    }

    @PostMapping("/register")
    public String registrarUsuario(@ModelAttribute Usuario usuario, Model model) {
        usuario.setRoles(Set.of("ROLE_ADMIN"));

    usuarioService.guardarUsuario(usuario);

    model.addAttribute("mensaje", "Usuario registrado correctamente");
    return "success";
    }
        
        @GetMapping("/home")
        public String homePage() {
        return "home";
    }

}