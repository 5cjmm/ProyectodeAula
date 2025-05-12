package com.ShopMaster.Controller;

import com.ShopMaster.Model.Deuda;
import com.ShopMaster.Service.DeudaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/deudas")
public class DeudaController {

    @Autowired
    private DeudaService deudaService;

    // Mostrar la vista Deudas.html
    @GetMapping
    public String mostrarVistaDeudas(Model model) {
        List<Deuda> deudas = deudaService.obtenerTodasLasDeudas();
        model.addAttribute("deudas", deudas);
        return "Deudas"; // archivo Deudas.html
    }

    // Registrar nueva deuda desde el modal en PuntoVenta
    /*@PostMapping("/registrar")
    public String registrarDeuda(@ModelAttribute Deuda deuda, BindingResult result, Model model) {
        if (result.hasErrors()) {
            result.getAllErrors().forEach(error -> System.out.println("❌ Error de binding: " + error));
            model.addAttribute("error", "No se pudo registrar la deuda");
            return "redirect:/tendero/PuntoVenta";
        }

        deuda.setFechaVenta(LocalDateTime.now());
        deuda.setEstado("NO_PAGADA");
        deuda.setTotalRestante(deuda.getTotal());
        deudaService.registrarDeuda(deuda);
        return "redirect:/Deudas"; // redirige a Deudas.html
    }*/
    @PostMapping("/registrar")
    public String registrarDeuda(@ModelAttribute Deuda deuda) {
        deuda.setFechaVenta(LocalDateTime.now());
        double total = deuda.getProductos().stream()
                .mapToDouble(p -> p.getPrecio() * p.getCantidad())
                .sum();
        deuda.setTotal(total);
        deuda.setTotalRestante(total);
        deuda.setEstado("NO_PAGADA");

        deudaService.registrarDeuda(deuda);
        return "redirect:/deudas";
    }


    // Registrar abono a una deuda
    @PostMapping("/abonar/{id}")
    public String abonarDeuda(@PathVariable String id, @RequestParam double monto) {
        deudaService.registrarAbono(id, monto);
        return "redirect:/Deudas";
    }
}
