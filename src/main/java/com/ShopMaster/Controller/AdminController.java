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

import com.ShopMaster.Model.Productos;
import com.ShopMaster.Model.Proveedores;
import com.ShopMaster.Model.Venta;
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
        List<Proveedores> proveedores = proveedorRepository.findAll();
        model.addAttribute("proveedores", proveedores);
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
        // Obtener la lista de ventas
        List<Venta> ventas = ventaService.obtenerTodaslasVentas();

        // Configurar la respuesta para descargar el archivo PDF
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"InformeVentas.pdf\"");

        // Crear el documento PDF
        Document document = new Document();
        PdfWriter.getInstance(document, response.getOutputStream());

        // Abrir el documento PDF
        document.open();

        // Crear la tabla de ventas
        PdfPTable tablaVenta = new PdfPTable(4);

        // Agregar las cabeceras
        tablaVenta.addCell("Nombre");
        tablaVenta.addCell("Cantidad");
        tablaVenta.addCell("Precio");
        tablaVenta.addCell("Total");

        // Llenar la tabla con las ventas
        for (Venta venta : ventas) {
            tablaVenta.addCell(venta.getNombre());
            tablaVenta.addCell(String.valueOf(venta.getCantidad()));
            tablaVenta.addCell(String.valueOf(venta.getPrecio()));
            tablaVenta.addCell(String.valueOf(venta.getTotal()));
        }

        // Agregar la tabla al documento
        document.add(tablaVenta);

        // Cerrar el documento
        document.close();
    }
    
}