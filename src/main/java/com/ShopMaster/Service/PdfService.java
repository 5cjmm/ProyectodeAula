package com.ShopMaster.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.ShopMaster.Model.ProductoVendido;
import com.ShopMaster.Model.Venta;
import com.ShopMaster.Repository.VentaRepository;
import com.ShopMaster.Repository.UsuarioRepository;
import com.ShopMaster.Model.Tienda;
import com.ShopMaster.Model.Usuario;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.net.URL;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PdfService {

    private final VentaRepository ventaRepository;
    private final UsuarioRepository usuarioRepository;

    public byte[] generarReciboVenta(String ventaId) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document();
        try {
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Image logo = loadLogo();

            // Formatos
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.DARK_GRAY);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

            NumberFormat cop = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date fecha = venta.getFecha() != null ? venta.getFecha() : new Date();

            // Encabezado reorganizado: logo | (tienda nombre + título) centrados | metadata alineada a la derecha
            String tiendaNombre = getTiendaNombre(venta.getTiendaId(), "-");

            
            PdfPTable topBar = new PdfPTable(2);
            topBar.setWidthPercentage(100);
            topBar.setWidths(new float[]{1f, 1f});
            PdfPCell topLeft = new PdfPCell(new Phrase("ID Venta: " + venta.getId(), labelFont));
            topLeft.setBorder(Rectangle.NO_BORDER);
            topLeft.setHorizontalAlignment(Element.ALIGN_LEFT);
            topBar.addCell(topLeft);
            PdfPCell topRight = new PdfPCell(new Phrase(sdf.format(fecha), normalFont));
            topRight.setBorder(Rectangle.NO_BORDER);
            topRight.setHorizontalAlignment(Element.ALIGN_RIGHT);
            topBar.addCell(topRight);
            doc.add(topBar);
            doc.add(new Paragraph("\n"));

            PdfPTable header = new PdfPTable(3);
            header.setWidthPercentage(100);
            header.setWidths(new float[]{1.5f, 3f, 1.5f});

            // Left: logo
            PdfPCell logoCell = new PdfPCell();
            if (logo != null) {
                logoCell = new PdfPCell(logo, false);
            }
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            header.addCell(logoCell);

            // Center: tienda nombre (arriba) y título centrado debajo
            PdfPTable center = new PdfPTable(1);
            center.setWidthPercentage(100);
            Font storeFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);
            PdfPCell storeNameCell = new PdfPCell(new Phrase(tiendaNombre, storeFont));
            storeNameCell.setBorder(Rectangle.NO_BORDER);
            storeNameCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            center.addCell(storeNameCell);

            Font receiptTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.BLACK);
            PdfPCell centerTitleCell = new PdfPCell(new Phrase("Recibo de Venta", receiptTitleFont));
            centerTitleCell.setBorder(Rectangle.NO_BORDER);
            centerTitleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            center.addCell(centerTitleCell);

            // Mostrar el nombre del usuario que realizó la venta (vendedor)
            Font vendorFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
            String vendedor = "-";
            try {
                if (venta.getUsuarioId() != null) {
                    java.util.Optional<Usuario> uOpt = usuarioRepository.findById(venta.getUsuarioId());
                    if (uOpt.isPresent()) vendedor = uOpt.get().getUsername();
                }
                if ("-".equals(vendedor)) {
                    // fallback: usuario que solicita el PDF (si está autenticado)
                    org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                    if (auth != null && auth.getName() != null) vendedor = auth.getName();
                }
            } catch (Exception ex) {
                // ignore and leave vendedor as '-'
            }
            PdfPCell vendorCell = new PdfPCell(new Phrase("Atendido por: " + vendedor, vendorFont));
            vendorCell.setBorder(Rectangle.NO_BORDER);
            vendorCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            center.addCell(vendorCell);

            PdfPCell centerCell = new PdfPCell(center);
            centerCell.setBorder(Rectangle.NO_BORDER);
            centerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            header.addCell(centerCell);

            // Right: dejar espacio para metadata (ID/Fecha movidos al topBar)
            PdfPCell rightCell = new PdfPCell(new Phrase(""));
            rightCell.setBorder(Rectangle.NO_BORDER);
            rightCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            header.addCell(rightCell);

            doc.add(header);
            doc.add(new Paragraph("\n"));

            // Tabla de productos
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.2f, 3.5f, 1f, 1.2f, 1.4f});

            addHeaderCell(table, "Código");
            addHeaderCell(table, "Producto");
            addHeaderCell(table, "Cant.");
            addHeaderCell(table, "P. Unit.");
            addHeaderCell(table, "Subtotal");

            double total = 0.0;
            if (venta.getProductos() != null) {
                for (ProductoVendido p : venta.getProductos()) {
                    double unit = p.getPrecioUnitario() > 0 ? p.getPrecioUnitario() : p.getPrecio();
                    double sub = p.getSubtotal() > 0 ? p.getSubtotal() : unit * p.getCantidad();
                    total += sub;

                    addCell(table, p.getCodigo(), normalFont);
                    addCell(table, p.getNombre(), normalFont);
                    addCell(table, String.valueOf(p.getCantidad()), normalFont, Element.ALIGN_RIGHT);
                    addCell(table, cop.format(unit), normalFont, Element.ALIGN_RIGHT);
                    addCell(table, cop.format(sub), normalFont, Element.ALIGN_RIGHT);
                }
            }

            doc.add(table);

            doc.add(new Paragraph("\n"));

            // Totales
            PdfPTable totals = new PdfPTable(2);
            totals.setWidthPercentage(40);
            totals.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totals.setWidths(new float[]{1.2f, 1.2f});
            PdfPCell empty = new PdfPCell(new Phrase(""));
            empty.setBorder(Rectangle.NO_BORDER);
            totals.addCell(empty);
            PdfPCell totalCell = new PdfPCell(new Phrase("Total: " + cop.format(venta.getTotal() > 0 ? venta.getTotal() : total), labelFont));
            totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalCell.setBorder(Rectangle.NO_BORDER);
            totals.addCell(totalCell);
            doc.add(totals);

            doc.add(new Paragraph("\n\nGracias por su compra.", normalFont));
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF", e);
        } finally {
            doc.close();
        }
        return baos.toByteArray();
    }

    public byte[] generarInformeVentasDia(String tiendaId, Date desde, Date hasta, String fechaLabel) {
        java.util.List<Venta> ventas = ventaRepository.findByTiendaIdAndFechaBetween(tiendaId, desde, hasta);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document();
        try {
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Image logo = loadLogo();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.BLACK);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            NumberFormat cop = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"));
            Paragraph title = new Paragraph("Informe de Ventas - " + (fechaLabel != null ? fechaLabel : ""), titleFont);

            // Construir encabezado con logo a la izquierda y tienda nombre + título centrado a la derecha
            String tiendaNombre = getTiendaNombre(tiendaId, "-");

            PdfPTable header = new PdfPTable(2);
            header.setWidthPercentage(100);
            header.setWidths(new float[]{1.5f, 4f});

            PdfPCell logoCell = new PdfPCell();
            if (logo != null) {
                logoCell = new PdfPCell(logo, false);
            }
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            header.addCell(logoCell);

            // Right side: tienda nombre arriba, título centrado debajo
            PdfPTable right = new PdfPTable(1);
            right.setWidthPercentage(100);

            Font storeFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);
            PdfPCell storeNameCell = new PdfPCell(new Phrase(tiendaNombre, storeFont));
            storeNameCell.setBorder(Rectangle.NO_BORDER);
            storeNameCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            right.addCell(storeNameCell);

            PdfPCell titleCell = new PdfPCell(new Phrase(title));
            titleCell.setBorder(Rectangle.NO_BORDER);
            titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            right.addCell(titleCell);

            // (opcional) info adicional de la tienda eliminada para evitar duplicado del nombre

            PdfPCell rightCell = new PdfPCell(right);
            rightCell.setBorder(Rectangle.NO_BORDER);
            rightCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            header.addCell(rightCell);

            doc.add(header);
            doc.add(new Paragraph("\n"));

            // Tabla: Producto | Cant. | P.Unit | Subtotal | Fecha
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3.5f, 1f, 1.2f, 1.4f, 1.6f});

            PdfPCell h1 = new PdfPCell(new Phrase("Producto", headerFont));
            h1.setBackgroundColor(new Color(10,145,178)); h1.setHorizontalAlignment(Element.ALIGN_CENTER); h1.setPadding(6f);
            PdfPCell h2 = new PdfPCell(new Phrase("Cant.", headerFont));
            h2.setBackgroundColor(new Color(10,145,178)); h2.setHorizontalAlignment(Element.ALIGN_CENTER); h2.setPadding(6f);
            PdfPCell h3 = new PdfPCell(new Phrase("P. Unit.", headerFont));
            h3.setBackgroundColor(new Color(10,145,178)); h3.setHorizontalAlignment(Element.ALIGN_CENTER); h3.setPadding(6f);
            PdfPCell h4 = new PdfPCell(new Phrase("Subtotal", headerFont));
            h4.setBackgroundColor(new Color(10,145,178)); h4.setHorizontalAlignment(Element.ALIGN_CENTER); h4.setPadding(6f);
            PdfPCell h5 = new PdfPCell(new Phrase("Fecha", headerFont));
            h5.setBackgroundColor(new Color(10,145,178)); h5.setHorizontalAlignment(Element.ALIGN_CENTER); h5.setPadding(6f);
            table.addCell(h1); table.addCell(h2); table.addCell(h3); table.addCell(h4); table.addCell(h5);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            double total = 0.0;
            for (Venta v : ventas) {
                Date f = v.getFecha() != null ? v.getFecha() : new Date();
                if (v.getProductos() == null) continue;
                for (ProductoVendido p : v.getProductos()) {
                    double unit = p.getPrecioUnitario() > 0 ? p.getPrecioUnitario() : p.getPrecio();
                    double sub = p.getSubtotal() > 0 ? p.getSubtotal() : unit * p.getCantidad();
                    total += sub;

                    addCell(table, p.getNombre(), normalFont);
                    addCell(table, String.valueOf(p.getCantidad()), normalFont, Element.ALIGN_RIGHT);
                    addCell(table, cop.format(unit), normalFont, Element.ALIGN_RIGHT);
                    addCell(table, cop.format(sub), normalFont, Element.ALIGN_RIGHT);
                    addCell(table, sdf.format(f), normalFont, Element.ALIGN_CENTER);
                }
            }

            doc.add(table);

            doc.add(new Paragraph("\n"));
            PdfPTable totals = new PdfPTable(2);
            totals.setWidthPercentage(40);
            totals.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totals.setWidths(new float[]{1.2f, 1.2f});
            PdfPCell empty = new PdfPCell(new Phrase(""));
            empty.setBorder(Rectangle.NO_BORDER);
            totals.addCell(empty);
            PdfPCell totalCell = new PdfPCell(new Phrase("TOTAL: " + cop.format(total), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK)));
            totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalCell.setBorder(Rectangle.NO_BORDER);
            totals.addCell(totalCell);
            doc.add(totals);

        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF de informe", e);
        } finally {
            doc.close();
        }
        return baos.toByteArray();
    }

    @SuppressWarnings("unused")
    private void addMetaRow(PdfPTable meta, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell l = new PdfPCell(new Phrase(label, labelFont));
        l.setBorder(Rectangle.NO_BORDER);
        PdfPCell v = new PdfPCell(new Phrase(value != null ? value : "-", valueFont));
        v.setBorder(Rectangle.NO_BORDER);
        meta.addCell(l);
        meta.addCell(v);
    }

    private void addHeaderCell(PdfPTable table, String text) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(text, headerFont));
        cell.setBackgroundColor(new Color(10, 145, 178)); // cyan-ish
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6f);
        table.addCell(cell);
    }

    /**
     * Carga el logo desde el classpath (/static/images/logo.png).
     * Devuelve null si el recurso no existe o ocurre un error.
     */
    private Image loadLogo() {
        try {
            URL url = getClass().getResource("/static/images/logo.png");
            if (url == null) return null;
            Image img = Image.getInstance(url);
            // Ajustar tamaño un poco más grande para encabezado
            img.scaleToFit(180f, 90f);
            img.setAlignment(Element.ALIGN_CENTER);
            return img;
        } catch (Exception e) {
            // No hacer fallar la generación del PDF si no se puede cargar la imagen
            return null;
        }
    }

    /**
     * Busca el nombre de la tienda por su id recorriendo los usuarios y sus tiendas embebidas.
     * Si no encuentra nombre, devuelve el fallback proporcionado o el id.
     */
    private String getTiendaNombre(String tiendaId, String fallback) {
        if (tiendaId == null) return fallback != null ? fallback : "-";
        try {
            java.util.List<Usuario> usuarios = usuarioRepository.findAll();
            for (Usuario u : usuarios) {
                if (u.getTiendas() == null) continue;
                for (Tienda t : u.getTiendas()) {
                    if (t == null || t.getId() == null) continue;
                    if (tiendaId.equals(t.getId())) {
                        return t.getNombre() != null ? t.getNombre() : (fallback != null ? fallback : t.getId());
                    }
                }
            }
        } catch (Exception e) {
            // Si algo falla, retornamos fallback
        }
        return fallback != null ? fallback : tiendaId;
    }

    private void addCell(PdfPTable table, String text, Font font) {
        addCell(table, text, font, Element.ALIGN_LEFT);
    }

    private void addCell(PdfPTable table, String text, Font font, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setHorizontalAlignment(align);
        cell.setPadding(5f);
        table.addCell(cell);
    }
}
