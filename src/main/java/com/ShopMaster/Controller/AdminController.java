package com.ShopMaster.Controller;
import java.awt.Color;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ShopMaster.Model.Venta;
import com.ShopMaster.Repository.ProductoRepositoryCustom;
import com.ShopMaster.Repository.ProveedorRepository;
import com.ShopMaster.Repository.VentaRepository;
import com.ShopMaster.Service.PdfCounterService;
import com.ShopMaster.Service.ProductosService;
import com.ShopMaster.Service.ProveedorService;
import com.ShopMaster.Service.UsuarioService;
import com.ShopMaster.Service.VentaService;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
private ProductoRepositoryCustom productoRepositoryCustom;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ProveedorService proveedorService;

    @Autowired
    private PdfCounterService counterService;

    @Autowired
    @SuppressWarnings("unused")
    private ProveedorRepository proveedorRepository;

    private final VentaService ventaService;
    
    @SuppressWarnings("unused")
    private final ProductosService productosService;

    @Autowired
    private VentaRepository ventaRepository;

    public AdminController(ProductosService productosService, VentaService ventaService) {
        this.productosService = productosService;
        this.ventaService = ventaService;
        
    }

    

  /*   @GetMapping("/Inventario")
    public String listar(Model model) {
        List<ProductoConProveedores> productos = productoRepositoryCustom.obtenerProductosConProveedores();
        model.addAttribute("productos", productos);

        //model.addAttribute("proveedores", proveedorService.obtenerTodosLosProveedores());
        model.addAttribute("nuevoProducto", new Productos());

        return "Inventario";
    }

    @GetMapping("/RegistroProveedor")
    public String mostrarProveedores(Model model) {
        //model.addAttribute("proveedores", proveedorService.obtenerTodosLosProveedores());
        model.addAttribute("nuevoProveedor", new Proveedor());
        return "RegistroProveedor";
    }


    @GetMapping("/InformeVentas")
    public String mostrarInformeVentas(Model model) {
        List<Venta> listadoVenta = ventaService.obtenerTodaslasVentas();
        model.addAttribute("ventas", listadoVenta);
        return "InformeVentas";
    }
    
    @GetMapping("/RegistroTendero")
    public String mostrarFormularioDeRegistro(Model model) {
        model.addAttribute("usuarios", usuarioService.obtenerTodosLosUsuarios());
        model.addAttribute("usuario", new Usuario()); 
        return "RegistroTendero";  
    } */

    @GetMapping("/pdf")
public void generarPDF(
        @RequestParam(value = "fecha", required = false)
        @DateTimeFormat(pattern = "yyyy-MM-dd") Date fecha,
        HttpServletResponse response) throws DocumentException, IOException {

    List<Venta> ventas;

    if (fecha != null) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fecha);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date inicio = calendar.getTime();
        calendar.add(Calendar.DATE, 1);
        Date fin = calendar.getTime();

       // ventas = ventaRepository.findByFechaBetween(inicio, fin);
    } else {
        ventas = ventaService.obtenerTodaslasVentas();
    }

    response.setContentType("application/pdf");
    response.setHeader("Content-Disposition", "attachment; filename=\"InformeDeVentas.pdf\"");

        try (Document document = new Document(PageSize.A4, 36, 36, 50, 50)) {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();
            
            Font tituloFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Font encabezadoFont = new Font(Font.HELVETICA, 12, Font.BOLD);
            Font contenidoFont = new Font(Font.HELVETICA, 11);
            
            Paragraph titulo = new Paragraph("Informe de Ventas", tituloFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(20f);
            document.add(titulo);
            
            PdfPTable tablaVenta = new PdfPTable(5);
            tablaVenta.setWidthPercentage(100);
            tablaVenta.setWidths(new float[] {3, 1.2f, 1.5f, 1.8f, 2.5f});
            tablaVenta.setSpacingBefore(10f);
            
            String[] encabezados = {"Nombre", "Cant.", "P. Unit (COP)", "Total (COP)", "Fecha"};
            for (String enc : encabezados) {
                PdfPCell celda = new PdfPCell(new Phrase(enc, encabezadoFont));
                celda.setHorizontalAlignment(Element.ALIGN_CENTER);
                celda.setBackgroundColor(Color.LIGHT_GRAY);
                celda.setPadding(5f);
                tablaVenta.addCell(celda);
            }
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            int totalCantidad = 0;
            double totalMonto = 0;
            
          /*   for (Venta venta : ventas) {
                List<ProductoVendido> productos = venta.getProductos(); // Asegúrate de tener el getter correcto
                
                for (ProductoVendido producto : productos) {
                    tablaVenta.addCell(new Phrase(producto.getNombre(), contenidoFont));
                    tablaVenta.addCell(new Phrase(String.valueOf(producto.getCantidad()), contenidoFont));
                    tablaVenta.addCell(new Phrase(String.format("%.2f", producto.getPrecio()), contenidoFont));
                    double totalProducto = producto.getCantidad() * producto.getPrecio();
                    tablaVenta.addCell(new Phrase(String.format("%.2f", totalProducto), contenidoFont));
                    tablaVenta.addCell(new Phrase(sdf.format(venta.getFecha()), contenidoFont));
                    
                    totalCantidad += producto.getCantidad();
                    totalMonto += totalProducto;
                }
            }*/
            
            document.add(new Paragraph("\n"));
            Paragraph resumenTitulo = new Paragraph("Resumen de Ventas", encabezadoFont);
            resumenTitulo.setSpacingBefore(10f);
            document.add(resumenTitulo);
            
            PdfPTable tablaResumen = new PdfPTable(2);
            tablaResumen.setWidthPercentage(50);
            tablaResumen.setHorizontalAlignment(Element.ALIGN_LEFT);
            tablaResumen.setSpacingBefore(10f);
            tablaResumen.setWidths(new float[]{3f, 2f});
            
            tablaResumen.addCell(new PdfPCell(new Phrase("Detalle", encabezadoFont)) {{
                setBackgroundColor(Color.LIGHT_GRAY);
                setPadding(5f);
            }});
            tablaResumen.addCell(new PdfPCell(new Phrase("Valor", encabezadoFont)) {{
                setBackgroundColor(Color.LIGHT_GRAY);
                setPadding(5f);
            }});
            
            tablaResumen.addCell(new Phrase("Cantidad total de productos", contenidoFont));
            tablaResumen.addCell(new Phrase(String.valueOf(totalCantidad), contenidoFont));
            
            tablaResumen.addCell(new Phrase("Monto total vendido", contenidoFont));
            tablaResumen.addCell(new Phrase(String.format("$%.2f", totalMonto), contenidoFont));
            
            tablaResumen.addCell(new Phrase("Número de transacciones", contenidoFont));
        //    tablaResumen.addCell(new Phrase(String.valueOf(ventas.size()), contenidoFont));
            
            document.add(tablaResumen);
            document.add(tablaVenta);
        }

    counterService.increment();
}




    @GetMapping("/count")
    public Map<String, Long> getCount() {
        long count = counterService.getCurrentCount();
        return Map.of("count", count);
    }

   /* @GetMapping("/InformeVentas/filtrar")
    public String filtrarPorFecha(@RequestParam("fecha") @DateTimeFormat(pattern = "yyyy-MM-dd") Date fecha, Model model) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fecha);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date inicio = calendar.getTime();

        calendar.add(Calendar.DATE, 1);
        Date fin = calendar.getTime();

        List<Venta> ventas = ventaRepository.findByFechaBetween(inicio, fin);
        model.addAttribute("ventas", ventas);
        model.addAttribute("filtroFecha", fecha);
        return "InformeVentas";*/

    }

    
