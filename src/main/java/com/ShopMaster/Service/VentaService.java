package com.ShopMaster.Service;

import java.util.*;
import java.time.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.DateOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import com.ShopMaster.Model.Venta;
import com.ShopMaster.Repository.VentaRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class VentaService {

    private final VentaRepository ventaRepository;
    private final MongoTemplate mongoTemplate;

    public List<Venta> obtenerTodaslasVentas() {
        return ventaRepository.findAll();
    }

    public void eliminarVenta(String id) {
        ventaRepository.deleteById(id);
    }

    // Método utilizado por el controlador: registrarVenta
    public Venta registrarVenta(Venta venta) {
        // Calcular total de la venta en el servidor para evitar registros con total=0
        if (venta != null) {
            double totalCalculado = 0.0;
            if (venta.getProductos() != null) {
                for (com.ShopMaster.Model.ProductoVendido pv : venta.getProductos()) {
                    // Preferir subtotal si viene; si no, calcularlo a partir de cantidad * precioUnitario
                    double precio = pv.getPrecioUnitario() > 0 ? pv.getPrecioUnitario() : pv.getPrecio();
                    double subtotal = pv.getSubtotal() > 0 ? pv.getSubtotal() : (precio * pv.getCantidad());
                    // Asegurar que el subtotal quede persistido
                    pv.setSubtotal(subtotal);
                    totalCalculado += subtotal;
                }
            }

            // Si el cliente no envió total o envió 0/negativo, usar el calculado
            if (venta.getTotal() <= 0) {
                venta.setTotal(totalCalculado);
            }

            // Si no se envió fecha, usar la del servidor (necesaria para métricas "ventas de hoy")
            if (venta.getFecha() == null) {
                venta.setFecha(new java.util.Date());
            }
        }

        return ventaRepository.save(venta);
    }

    // Método con nombre alternativo (si otras partes usan guardarVenta)
    public Venta guardarVenta(Venta venta) {
        return registrarVenta(venta);
    }

    public Page<Venta> obtenerVentasPorTienda(String tiendaId, int page, int size) {
        // Ordenar por fecha descendente para mostrar las ventas más recientes primero
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fecha"));
        return ventaRepository.findByTiendaId(tiendaId, pageable);
    }

    // Obtener últimas N ventas (ordenadas por fecha desc)
    public java.util.List<Venta> obtenerUltimasVentas(String tiendaId, int limite) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limite,
                Sort.by(Sort.Direction.DESC, "fecha"));
        return ventaRepository.findByTiendaId(tiendaId, pageable).getContent();
    }

    // Ventas mensuales (últimos N meses) agrupadas por año-mes
    public List<Map<String, Object>> obtenerVentasMensuales(String tiendaId, int meses) {
    int window = Math.max(1, Math.min(meses, 24)); // limitar a 24 por seguridad

    // Usar YearMonth con zona horaria de Bogotá para evitar desfaces y off-by-one
    ZoneId zone = ZoneId.of("America/Bogota");
    YearMonth endYm = YearMonth.now(zone);             // mes actual
    YearMonth startYm = endYm.minusMonths(window - 1); // hace window-1 meses

    // Rango de fechas: [startYm-01 00:00, nextMonth(endYm)-01 00:00)
    Date from = Date.from(startYm.atDay(1).atStartOfDay(zone).toInstant());
    Date toExclusive = Date.from(endYm.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant());

    // Match por tienda y rango de fechas (usar < toExclusive para incluir completo el mes final)
    MatchOperation match = Aggregation.match(
        Criteria.where("tiendaId").is(tiendaId)
            .and("fecha").gte(from).lt(toExclusive));

    // Proyectar año-mes como cadena yyyy-MM usando zona horaria de Bogotá para evitar desfaces
    ProjectionOperation project = Aggregation.project("total")
        .and(DateOperators.DateToString.dateOf("fecha")
            .toString("%Y-%m")
            .withTimezone(DateOperators.Timezone.valueOf("America/Bogota")))
        .as("ym");

        // Agrupar por año-mes y sumar total
        GroupOperation group = Aggregation.group("ym").sum("total").as("total");

        Aggregation agg = Aggregation.newAggregation(
                match,
                project,
                group,
                Aggregation.sort(Sort.Direction.ASC, "_id")
        );

        AggregationResults<org.bson.Document> results = mongoTemplate.aggregate(agg, "ventas", org.bson.Document.class);
        Map<String, Double> totalsByYm = new HashMap<>();
        for (org.bson.Document d : results.getMappedResults()) {
            String ym = d.getString("_id");
            Number n = (Number) d.get("total");
            totalsByYm.put(ym, n != null ? n.doubleValue() : 0.0);
        }

        // Construir lista completa de meses del rango, con 0 cuando no hay ventas
        List<Map<String, Object>> out = new ArrayList<>();
        YearMonth iter = startYm;
        for (int i = 0; i < window; i++) {
            int y = iter.getYear();
            int m = iter.getMonthValue(); // 1..12
            String ym = String.format("%04d-%02d", y, m);
            double total = totalsByYm.getOrDefault(ym, 0.0);
            Map<String, Object> row = new HashMap<>();
            row.put("year", y);
            row.put("month", m);
            row.put("label", ym);
            row.put("total", total);
            out.add(row);
            iter = iter.plusMonths(1);
        }
        return out;
    }
}

