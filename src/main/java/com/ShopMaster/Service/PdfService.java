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
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PdfService {

    private final VentaRepository ventaRepository;

    public byte[] generarReciboVenta(String ventaId) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document();
        try {
            PdfWriter.getInstance(doc, baos);
            doc.open();

            // Formatos
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.BLACK);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.DARK_GRAY);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

            NumberFormat cop = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date fecha = venta.getFecha() != null ? venta.getFecha() : new Date();

            // Encabezado
            Paragraph title = new Paragraph("Recibo de Venta", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);
            doc.add(new Paragraph("\n"));

            PdfPTable meta = new PdfPTable(2);
            meta.setWidthPercentage(100);
            meta.setWidths(new float[]{1.2f, 3f});

            addMetaRow(meta, "ID Venta:", venta.getId(), labelFont, normalFont);
            addMetaRow(meta, "Fecha:", sdf.format(fecha), labelFont, normalFont);
            addMetaRow(meta, "Tienda:", venta.getTiendaId() != null ? venta.getTiendaId() : "-", labelFont, normalFont);
            doc.add(meta);

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

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.BLACK);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            NumberFormat cop = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"));

            Paragraph title = new Paragraph("Informe de Ventas - " + (fechaLabel != null ? fechaLabel : ""), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);
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
