package com.ShopMaster.Controller;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ShopMaster.Model.Usuario;
import com.ShopMaster.Service.UsuarioService;

@Controller
@RequestMapping("/admin")
public class UsuarioController {
@Autowired
    private UsuarioService usuarioService;

    @PostMapping("/Registro")
    public String registrarUsuario(@ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes) {
        usuario.setRoles(Set.of("ROLE_TENDERO"));
        usuarioService.guardarUsuario(usuario);
        redirectAttributes.addFlashAttribute("SuccessMessage", "Usuario registrado exitosamente");
        return "redirect:/admin/Registro";  
    }

    @PostMapping("/actualizar-tendero")
    public String actualizarUsuario(@ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes) {
        usuarioService.actualizarUsuario(usuario);
        redirectAttributes.addFlashAttribute("SuccessMessage", "¡Usuario actualizado exitosamente!");
        return "redirect:/admin/Registro";
    }

    @PostMapping("/eliminar-tendero/{id}")
    public String eliminarUsuario(@PathVariable String id, RedirectAttributes redirectAttributes) {
        usuarioService.eliminarUsuario(id);
        redirectAttributes.addFlashAttribute("SuccessMessage", "!Usuario eliminado exitosamente!");
        return "redirect:/admin/Registro";
    }
}
