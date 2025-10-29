package com.ShopMaster.Controller;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ShopMaster.Model.Deuda;
import com.ShopMaster.Model.Productos;
import com.ShopMaster.Model.Usuario;
import com.ShopMaster.Repository.DeudaRepository;
import com.ShopMaster.Repository.ProductosRepository;
import com.ShopMaster.Repository.VentaRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/tiendas")
public class TiendaViewController {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ProductosRepository productosRepository;

    @Autowired
    private DeudaRepository deudaRepository;

    @GetMapping
    public String showTiendasPage(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario.getRoles().contains("ROLE_TENDERO")) {
        if (usuario.getTiendas() != null && !usuario.getTiendas().isEmpty()) {
            String tiendaId = usuario.getTiendas().get(0).getId();
            return "redirect:/tiendas/" + tiendaId + "/dashboard";
        } else {
            return "redirect:/sin-tienda"; // En caso de que no tenga tienda asignada
        }
    }
        model.addAttribute("usuario", usuario); // se pasa a tiendas.html
        return "tiendas"; // busca templates/tiendas.html
    }

    @GetMapping("/{id}/dashboard")
    public String verDashboard(@PathVariable String id,
            @org.springframework.web.bind.annotation.RequestParam(value = "bajosPage", defaultValue = "0") int bajosPageParam,
            Model model) {
        model.addAttribute("tiendaId", id);

        // --- Ventas totales (sum total de la tienda) - sin límite de página
        Double sumAgg = ventaRepository.sumTotalByTiendaId(id);
        double ventasTotales = sumAgg != null ? sumAgg : 0.0;

        // --- Ventas hoy (cantidad de ventas en la fecha actual)
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date inicio = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date fin = cal.getTime();
        long ventasHoyCount = ventaRepository.countByTiendaIdAndFechaBetween(id, inicio, fin);

        // --- Productos en stock (suma de cantidades)
        List<Productos> productos = productosRepository.findByTiendaId(id);
        int productosEnStock = productos.stream().mapToInt(Productos::getCantidad).sum();

        // --- Deudas activas (estado distinto a PAGADA)
        long deudasActivas = deudaRepository.countByTiendaIdAndEstadoNotIgnoreCase(id, "PAGADA");

        // --- Productos con stock bajo (ej: <=10) — paginados
        final int pageSize = 5;
        org.springframework.data.domain.Page<Productos> bajosPage = productosRepository.findByTiendaIdAndCantidadLessThanEqual(id, 10, org.springframework.data.domain.PageRequest.of(Math.max(0, bajosPageParam), pageSize));

        model.addAttribute("ventasTotales", ventasTotales);
        model.addAttribute("ventasTotalesFormatted", String.format("$%,.2f", ventasTotales));
        model.addAttribute("productosEnStock", productosEnStock);
        model.addAttribute("ventasHoy", ventasHoyCount);
        model.addAttribute("deudasActivas", deudasActivas);
        model.addAttribute("bajosPage", bajosPage);

        // --- Deudas recientes (últimas 5)
        List<Deuda> deudasRecientes = deudaRepository.findTop5ByTiendaIdOrderByFechaVentaDesc(id);
        model.addAttribute("deudasRecientes", deudasRecientes);

        // recientes: últimas 5 ventas (ordenadas por fecha descendente)
        // Nota: reemplazado por deudasRecientes para la sección de recientes
        return "Dashboard";
    }

    @GetMapping("/{id}/inventario")
    public String verInventario(@PathVariable String id, Model model) {
        model.addAttribute("tiendaId", id); // se pasa a Inventario.html
        return "Inventario"; // templates/Inventario.html
    }

    @GetMapping("/{id}/proveedores")
    public String verProveedor(@PathVariable String id, Model model) {
        model.addAttribute("tiendaId", id); // se pasa a RegistroProveedor.html
        return "RegistroProveedor"; // templates/RegistroProveedor.html
    }

    @GetMapping("/{id}/deudas")
    public String verDeudas(@PathVariable String id, Model model) {
        model.addAttribute("tiendaId", id); // se pasa a Deudas.html
        return "Deudas"; // templates/Deudas.html
    }

    @GetMapping("/{id}/puntoventa")
    public String verPuntoVenta(@PathVariable String id, Authentication authentication, Model model) {
        model.addAttribute("tiendaId", id);
        model.addAttribute("rolUsuario", authentication.getAuthorities().iterator().next().getAuthority());
        return "PuntoVenta"; // templates/PuntoVenta.html
    }

    @GetMapping("/{id}/informe")
    public String verInforme(@PathVariable String id, Model model) {
        model.addAttribute("tiendaId", id); // se pasa a Informe.html
        return "InformeVentas"; // templates/Informe.html
    }

    @GetMapping("/{id}/tendero")
    @PreAuthorize("hasRole('ADMIN')")
    public String verTendero(@PathVariable String id, HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario.getRoles().contains("ROLE_TENDERO")) {
        if (usuario.getTiendas() != null && !usuario.getTiendas().isEmpty()) {
            String tiendaId = usuario.getTiendas().get(0).getId();
            return "redirect:/tiendas/" + tiendaId + "/dashboard";
        } else {
            return "redirect:/sin-tienda"; // En caso de que no tenga tienda asignada
        }
    }
        model.addAttribute("tiendaId", id); // se pasa a Tendero.html
        return "RegistroTendero"; // templates/Tendero.html

    }

    @GetMapping("/{id}/perfil")
    public String verPerfil(@PathVariable String id, Model model) {
        model.addAttribute("tiendaId", id);
        return "Perfil"; // templates/Perfil.html
    }

    @GetMapping({"/{id}/prediccion", "/tendero/tiendas/{id}/prediccion"})
    public String verPrediccion(@PathVariable String id, Model model) {
        model.addAttribute("tiendaId", id); // se pasa a Prediccion.html
        return "Prediccion"; // templates/Prediccion.html
    }

    // --- Endpoint JSON para paginación de inventario bajo (AJAX)
    @GetMapping("/{id}/dashboard/bajos")
    @org.springframework.web.bind.annotation.ResponseBody
    public java.util.Map<String, Object> bajosAjax(@PathVariable String id,
            @org.springframework.web.bind.annotation.RequestParam(value = "bajosPage", defaultValue = "0") int bajosPageParam) {
        final int pageSize = 5;
        org.springframework.data.domain.Page<Productos> bajosPage = productosRepository
                .findByTiendaIdAndCantidadLessThanEqual(id, 10,
                        org.springframework.data.domain.PageRequest.of(Math.max(0, bajosPageParam), pageSize));

        java.util.List<java.util.Map<String, Object>> content = bajosPage.getContent().stream()
                .map(p -> {
                    java.util.Map<String, Object> m = new java.util.HashMap<>();
                    m.put("nombre", p.getNombre());
                    m.put("cantidad", p.getCantidad());
                    return m;
                })
                .collect(java.util.stream.Collectors.toList());

        return java.util.Map.of(
                "content", content,
                "page", bajosPage.getNumber(),
                "totalPages", bajosPage.getTotalPages(),
                "totalElements", bajosPage.getTotalElements(),
                "hasNext", bajosPage.hasNext(),
                "hasPrevious", bajosPage.hasPrevious()
        );
    }
}
