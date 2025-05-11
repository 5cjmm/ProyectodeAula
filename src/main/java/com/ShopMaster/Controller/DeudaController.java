package com.ShopMaster.Controller;

import com.ShopMaster.Model.Deuda;
import com.ShopMaster.Service.DeudaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/deudas")
public class DeudaController {

    @Autowired
    private DeudaService deudaService;

    @GetMapping
    public String listarDeudas(Model model) {
        List<Deuda> deudas = deudaService.obtenerTodasLasDeudas();
        model.addAttribute("deudas", deudas);
        return "lista"; //archivo lista.html
    }

    @PostMapping("/registrar")
    public String registrarDeuda(@ModelAttribute Deuda deuda) {
        deudaService.registrarDeuda(deuda);
        return "redirect:/lista";
    }

    @PostMapping("/abonar/{id}")
    public String abonarDeuda(@PathVariable String id, @RequestParam double monto) {
        deudaService.registrarAbono(id, monto);
        return "redirect:/lista";
    }
}
