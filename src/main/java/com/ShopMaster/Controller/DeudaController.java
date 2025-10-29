package com.ShopMaster.Controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ShopMaster.Model.Abono;
import com.ShopMaster.Model.Deuda;
import com.ShopMaster.Service.DeudaService;

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

    @GetMapping("/historial/{id}")
    @ResponseBody
    public List<Abono> obtenerHistorialAbonos(@PathVariable String id) {
        return deudaService.obtenerDeudaPorId(id)
                .map(Deuda::getHistorialAbonos)
                .orElse(new ArrayList<>());
    }


    // Registrar abono a una deuda
    @PostMapping("/abonar/{id}")
    public String abonarDeuda(@PathVariable String id, @RequestParam double monto) {
        deudaService.registrarAbono(id, monto);
        return "redirect:/deudas";
    }

}
