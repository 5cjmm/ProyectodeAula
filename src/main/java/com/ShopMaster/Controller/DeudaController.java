package com.ShopMaster.Controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ShopMaster.Model.Abono;
import com.ShopMaster.Model.Deuda;
import com.ShopMaster.Repository.DeudaRepository;
import com.ShopMaster.Service.DeudaService;

@Controller
@RequestMapping("/deudas")
public class DeudaController {

    @Autowired
    private DeudaService deudaService;

    @Autowired
    private DeudaRepository deudaRepository;

    // Mostrar la vista Deudas.html
    @GetMapping
    public String mostrarVistaDeudas(Model model) {
        List<Deuda> deudas = deudaService.obtenerTodasLasDeudas();
        model.addAttribute("deudas", deudas);
        return "Deudas"; // archivo Deudas.html
    }

    // Registrar nueva deuda desde el modal en PuntoVenta
    @PostMapping("/registrar")
    public String registrarDeuda(@ModelAttribute Deuda deuda, RedirectAttributes redirectAttributes) {
        deuda.setFechaVenta(LocalDateTime.now());
        double total = deuda.getProductos().stream()
                .mapToDouble(p -> p.getPrecio() * p.getCantidad())
                .sum();
        deuda.setTotal(total);
        deuda.setTotalRestante(total);
        deuda.setEstado("NO_PAGADA");

        deudaService.registrarDeuda(deuda);
        redirectAttributes.addFlashAttribute("SuccessMessage", "Deuda registrada exitosamente");
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
public String abonarDeuda(@PathVariable String id, @RequestParam double monto, RedirectAttributes redirectAttributes) {
    // Obtener la deuda por su ID
    Optional<Deuda> deudaOptional = deudaService.obtenerDeudaPorId(id);

    // Verificar si la deuda existe
    if (deudaOptional.isEmpty()) {
        redirectAttributes.addFlashAttribute("error", "La deuda no existe.");
        return "redirect:/deudas";
    }

    Deuda deuda = deudaOptional.get();
    double totalRestante = deuda.getTotalRestante();

    // Validar que el monto del abono no sea mayor que el total restante
    if (monto > totalRestante) {
        redirectAttributes.addFlashAttribute("error", "El monto a abonar es mayor que el saldo restante de la deuda.");
        return "redirect:/deudas";
    }

    // Si la validación pasa, registrar el abono
    deudaService.registrarAbono(id, monto);
    
    redirectAttributes.addFlashAttribute("SuccessMessage", "Deuda abonada exitosamente");
    return "redirect:/deudas";
}


    @PostMapping("/eliminar/{id}")
public String eliminarDeuda(@PathVariable String id, RedirectAttributes redirectAttributes) {
    Optional<Deuda> deudaOptional = deudaService.obtenerDeudaPorId(id);

    if (deudaOptional.isEmpty()) {
        redirectAttributes.addFlashAttribute("error", "La deuda no existe.");
    } else {
        Deuda deuda = deudaOptional.get();
        if (!"PAGADA".equalsIgnoreCase(deuda.getEstado())) {
            redirectAttributes.addFlashAttribute("error", "Solo se pueden eliminar deudas pagadas.");
        } else {
            deudaService.eliminar(id);
            redirectAttributes.addFlashAttribute("SuccessMessage", "Deuda eliminada exitosamente.");
        }
    }

    return "redirect:/deudas";
}


@GetMapping("/filtrar")
public String filtrarDeudasPorFecha(@RequestParam("fecha") @DateTimeFormat(pattern = "yyyy-MM-dd") Date fecha, Model model) {
    // Establecer hora al inicio del día
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(fecha);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    Date inicio = calendar.getTime();

    // Fin del día
    calendar.add(Calendar.DATE, 1);
    Date fin = calendar.getTime();

    // Convertir a LocalDateTime para la consulta
    LocalDateTime fechaInicio = inicio.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    LocalDateTime fechaFin = fin.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

    // Consulta directa al repositorio
    List<Deuda> deudasFiltradas = deudaRepository.findByFechaVentaBetween(fechaInicio, fechaFin);

    model.addAttribute("deudas", deudasFiltradas);
    model.addAttribute("filtroFecha", fecha);
    return "Deudas"; // La vista que muestra la tabla
}



}