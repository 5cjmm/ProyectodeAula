package com.ShopMaster.Controller;
import com.ShopMaster.Service.VentaService;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ShopMaster.Model.ProductoConProveedores;
import com.ShopMaster.Model.Productos;
import com.ShopMaster.Model.Proveedor;
import com.ShopMaster.Model.Venta;
import com.ShopMaster.Repository.ProductoRepositoryCustom;
import com.ShopMaster.Repository.ProveedorRepository;
import com.ShopMaster.Service.ProductosService;
import com.ShopMaster.Service.ProveedorService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
private ProductoRepositoryCustom productoRepositoryCustom;

@Autowired
private ProveedorService proveedorService;

    @Autowired
    @SuppressWarnings("unused")
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
    public String listar(Model model) {
        List<ProductoConProveedores> productos = productoRepositoryCustom.obtenerProductosConProveedores();
        model.addAttribute("productos", productos);

        // Agregar proveedor y producto nuevo para el formulario
        model.addAttribute("proveedores", proveedorService.obtenerTodosLosProveedores());
        model.addAttribute("nuevoProducto", new Productos());

        return "Inventario";
    }


    @GetMapping("/InformeVentas")
        public String mostrarInformeVentas(Model model) {
            List<Venta> listadoVenta = ventaService.obtenerTodaslasVentas();
        model.addAttribute("venta", listadoVenta);
        return "InformeVentas";
    }
    
    @GetMapping("/pdf")
    public void generarPDF(HttpServletResponse response) throws DocumentException, IOException {
        List<Venta> ventas = ventaService.obtenerTodaslasVentas();

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"InformeDeVentas.pdf\"");

        Document document = new Document();
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        PdfPTable tablaVenta = new PdfPTable(4);

        tablaVenta.addCell("Nombre");
        tablaVenta.addCell("Cantidad");
        tablaVenta.addCell("Precio");
        tablaVenta.addCell("Total");

        for (Venta venta : ventas) {
            tablaVenta.addCell(venta.getNombre());
            tablaVenta.addCell(String.valueOf(venta.getCantidad()));
            tablaVenta.addCell(String.valueOf(venta.getPrecio()));
            tablaVenta.addCell(String.valueOf(venta.getTotal()));
        }

        document.add(tablaVenta);

        document.close();
    }
    
}