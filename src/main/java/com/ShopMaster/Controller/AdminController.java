package com.ShopMaster.Controller;
import com.ShopMaster.Service.VentaService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ShopMaster.Model.Productos;
import com.ShopMaster.Model.Proveedores;
import com.ShopMaster.Repository.ProveedorRepository;
import com.ShopMaster.Service.ProductosService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ProveedorRepository proveedorRepository;

    private final VentaService ventaService;
    private final ProductosService productosService;

    public AdminController(ProductosService productosService, VentaService ventaService) {
        this.productosService = productosService;
        this.ventaService = ventaService;
        
    }

    @GetMapping("/Dashboard")
        public String mostrarDashboard() {
        return "Dashboard";
    }

    @GetMapping("/Inventario")
    public String mostrarInventario(Model model) {
        model.addAttribute("productos", productosService.obtenerTodosLosProductos());
        List<Proveedores> proveedores = proveedorRepository.findAll(); // Obtener proveedores
        model.addAttribute("proveedores", proveedores);
        model.addAttribute("nuevoProducto", new Productos());
        return "Inventario";
    }

    @GetMapping("/InformeVentas")
        public String mostrarInformeVentas(Model model) {
        model.addAttribute("venta", ventaService.obtenerTodaslasVentas());
        return "InformeVentas";
    }
    
}