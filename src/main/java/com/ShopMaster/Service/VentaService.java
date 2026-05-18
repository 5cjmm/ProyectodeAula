package com.ShopMaster.Service;

import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.DateOperators;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import com.ShopMaster.Model.ProductoVendido;
import com.ShopMaster.Model.Productos;
import com.ShopMaster.Model.Venta;
import com.ShopMaster.Repository.ProductosRepository;
import com.ShopMaster.Repository.UsuarioRepository;
import com.ShopMaster.Repository.VentaRepository;

@Service
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductosRepository productosRepository;

    public List<Venta> obtenerTodaslasVentas() {
        return ventaRepository.findAll();
    }

    // ── Soft Delete ──────────────────────────────────────────────
    public void eliminarVenta(String id) {
        Venta v = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada: " + id));
        v.setActivo(false);
        ventaRepository.save(v);
    }

    public Venta registrarVenta(Venta venta) {
        if (venta != null) {
            double totalCalculado = 0.0;
            if (venta.getProductos() != null) {
                for (ProductoVendido pv : venta.getProductos()) {
                    double precio = pv.getPrecioUnitario() > 0 ? pv.getPrecioUnitario() : pv.getPrecio();
                    double subtotal = pv.getSubtotal() > 0 ? pv.getSubtotal() : (precio * pv.getCantidad());
                    pv.setSubtotal(subtotal);
                    totalCalculado += subtotal;
                }
            }

            if (venta.getTotal() <= 0) {
                venta.setTotal(totalCalculado);
            }

            if (venta.getFecha() == null) {
                venta.setFecha(new Date());
            }

            try {
                if (venta.getUsuarioId() == null) {
                    org.springframework.security.core.Authentication auth =
                            org.springframework.security.core.context.SecurityContextHolder
                                    .getContext().getAuthentication();
                    if (auth != null && auth.getName() != null) {
                        usuarioRepository.findByUsername(auth.getName())
                                .ifPresent(u -> venta.setUsuarioId(u.getId()));
                    }
                }
            } catch (Exception ex) {
                // no bloquear el guardado si falla la resolución del usuario
            }
        }

        if (venta != null && venta.getProductos() != null) {
            for (ProductoVendido pv : venta.getProductos()) {
                Productos producto = productosRepository.findById(pv.getProductoId())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + pv.getProductoId()));

                if (pv.getCantidad() > producto.getCantidad()) {
                    throw new RuntimeException("Cantidad insuficiente para: " + producto.getNombre());
                }

                producto.setCantidad(producto.getCantidad() - pv.getCantidad());
                productosRepository.save(producto);
            }
        }

        venta.setActivo(true);
        return ventaRepository.save(venta);
    }

    public Venta guardarVenta(Venta venta) {
        return registrarVenta(venta);
    }

    // ── Consultas con Soft Delete ────────────────────────────────
    public Page<Venta> obtenerVentasPorTienda(String tiendaId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fecha"));
        return ventaRepository.findByTiendaIdAndActivoTrue(tiendaId, pageable);
    }

    public Page<Venta> obtenerVentasPorRango(String tiendaId, Date desde, Date hasta, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fecha"));
        return ventaRepository.findByTiendaIdAndFechaBetweenAndActivoTrue(tiendaId, desde, hasta, pageable);
    }

    public Map<String, Object> obtenerResumenPorRango(String tiendaId, Date desde, Date hasta) {
        List<Venta> ventas = ventaRepository.findByTiendaIdAndFechaBetweenAndActivoTrue(tiendaId, desde, hasta);
        long totalVentas   = ventas.size();
        double totalMonto  = 0.0;
        long totalCantidad = 0L;

        for (Venta v : ventas) {
            totalMonto += v.getTotal();
            if (v.getProductos() != null) {
                for (ProductoVendido p : v.getProductos()) {
                    totalCantidad += p.getCantidad();
                }
            }
        }

        Map<String, Object> out = new HashMap<>();
        out.put("totalVentas",   totalVentas);
        out.put("totalCantidad", totalCantidad);
        out.put("totalMonto",    totalMonto);
        return out;
    }

    public List<Venta> obtenerUltimasVentas(String tiendaId, int limite) {
        Pageable pageable = PageRequest.of(0, limite, Sort.by(Sort.Direction.DESC, "fecha"));
        return ventaRepository.findByTiendaIdAndActivoTrue(tiendaId, pageable).getContent();
    }

    public List<Map<String, Object>> obtenerVentasMensuales(String tiendaId, int meses) {
        int window   = Math.max(1, Math.min(meses, 24));
        ZoneId zone  = ZoneId.of("America/Bogota");
        YearMonth endYm   = YearMonth.now(zone);
        YearMonth startYm = endYm.minusMonths(window - 1);

        Date from        = Date.from(startYm.atDay(1).atStartOfDay(zone).toInstant());
        Date toExclusive = Date.from(endYm.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant());

        // ✅ Filtrar solo ventas activas en la agregación
        MatchOperation match = Aggregation.match(
                Criteria.where("tiendaId").is(tiendaId)
                        .and("activo").is(true)
                        .and("fecha").gte(from).lt(toExclusive));

        ProjectionOperation project = Aggregation.project("total")
                .and(DateOperators.DateToString.dateOf("fecha")
                        .toString("%Y-%m")
                        .withTimezone(DateOperators.Timezone.valueOf("America/Bogota")))
                .as("ym");

        GroupOperation group = Aggregation.group("ym").sum("total").as("total");

        Aggregation agg = Aggregation.newAggregation(
                match, project, group,
                Aggregation.sort(Sort.Direction.ASC, "_id"));

        AggregationResults<org.bson.Document> results =
                mongoTemplate.aggregate(agg, "ventas", org.bson.Document.class);

        Map<String, Double> totalsByYm = new HashMap<>();
        for (org.bson.Document d : results.getMappedResults()) {
            String ym = d.getString("_id");
            Number n  = (Number) d.get("total");
            totalsByYm.put(ym, n != null ? n.doubleValue() : 0.0);
        }

        List<Map<String, Object>> out = new ArrayList<>();
        YearMonth iter = startYm;
        for (int i = 0; i < window; i++) {
            String ym    = String.format("%04d-%02d", iter.getYear(), iter.getMonthValue());
            double total = totalsByYm.getOrDefault(ym, 0.0);
            Map<String, Object> row = new HashMap<>();
            row.put("year",  iter.getYear());
            row.put("month", iter.getMonthValue());
            row.put("label", ym);
            row.put("total", total);
            out.add(row);
            iter = iter.plusMonths(1);
        }
        return out;
    }
}
