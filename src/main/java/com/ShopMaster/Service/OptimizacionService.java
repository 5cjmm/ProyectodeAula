package com.ShopMaster.Service;

import com.ShopMaster.Model.Productos;
import com.ShopMaster.Repository.ProductosRepository;
import com.ShopMaster.dto.OptimizacionRequest;
import com.ShopMaster.dto.OptimizacionResponse;

import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class OptimizacionService {

    @Autowired
    private ProductosRepository productosRepository;

    public OptimizacionResponse optimizar(OptimizacionRequest req) {

        // ── 1. Traer productos reales de la tienda desde MongoDB ──────────────
        //    Solo los que tengan stock disponible (cantidad > 0)
        List<Productos> productos = productosRepository
                .findByTiendaIdAndCantidadGreaterThan(req.getTiendaId(), 0);

        if (productos == null || productos.isEmpty()) {
            OptimizacionResponse r = new OptimizacionResponse();
            r.setEstado("sin_productos");
            r.setMensaje("No hay productos con stock en esta tienda.");
            return r;
        }

        int n = productos.size();

        // ── 2. Calcular ganancia unitaria real de cada producto ────────────────
        //    ganancia = precio de venta - costo de compra al proveedor
        //    Si costoCompra no fue registrado (es 0), se estima como 60% del precio
        double[] ganancias  = new double[n];
        double[] costos     = new double[n];   // para restricción de presupuesto
        double[] stock      = new double[n];   // límite superior de cada variable

        for (int i = 0; i < n; i++) {
            Productos p = productos.get(i);
            double costo = (p.getCostoCompra() != null && p.getCostoCompra().compareTo(BigDecimal.ZERO) > 0)
                    ? p.getCostoCompra().doubleValue()
                    : p.getPrecio() * 0.60;

            ganancias[i] = p.getPrecio() - costo;
            costos[i]    = costo;
            stock[i]     = p.getCantidad();    // máximo a vender = stock actual
        }

        // ── 3. Construir el modelo de Programación Lineal con ojAlgo ──────────
        ExpressionsBasedModel model = new ExpressionsBasedModel();

        //  Variables de decisión: xi = cuántas unidades vender del producto i
        //  Límite inferior: 0  |  Límite superior: stock disponible
        //  Peso (coeficiente en función objetivo): ganancia unitaria
        Variable[] x = new Variable[n];
        for (int i = 0; i < n; i++) {
            x[i] = model.addVariable("x" + i)
                        .lower(0)
                        .upper(stock[i])
                        .weight(ganancias[i]);
        }

        // Restricción 1: Capacidad total de ventas
        //   x1 + x2 + ... + xn <= capacidadVentas
        var rCap = model.addExpression("capacidad_ventas").upper(req.getCapacidadVentas());
        for (int i = 0; i < n; i++) rCap.set(x[i], 1.0);

        // Restricción 2: Presupuesto de reabastecimiento con proveedores
        //   costo1*x1 + costo2*x2 + ... <= presupuesto
        var rProv = model.addExpression("presupuesto_proveedores").upper(req.getPresupuesto());
        for (int i = 0; i < n; i++) rProv.set(x[i], costos[i]);

        // Restricción 3: Crédito disponible de clientes
        //   Se usa el costo unitario como peso para estimar exposición crediticia
        var rCred = model.addExpression("credito_clientes").upper(req.getCreditoDisponible());
        for (int i = 0; i < n; i++) rCred.set(x[i], costos[i] * 0.5);

        // Restricción 4: Inventario total (suma ponderada de unidades)
        //   Productos de mayor volumen pesan más (aquí peso = 1 por defecto;
        //   puedes personalizar según m³ o kg de cada producto)
        var rInv = model.addExpression("inventario_total").upper(req.getInventarioTotal());
        for (int i = 0; i < n; i++) rInv.set(x[i], 1.0);

        // ── 4. Resolver: MAXIMIZAR Z ──────────────────────────────────────────
        Optimisation.Result result = model.maximise();

        // ── 5. Verificar que se encontró solución óptima ──────────────────────
        if (!result.getState().isSuccess()) {
            OptimizacionResponse r = new OptimizacionResponse();
            r.setEstado("sin_solucion");
            r.setMensaje("No se encontró solución óptima. Intenta aumentar el presupuesto o la capacidad de ventas.");
            return r;
        }

        // ── 6. Construir la respuesta con los resultados ──────────────────────
        double gananciaTotal = 0;
        double usoCap        = 0;
        double usoProv       = 0;
        double usoCred       = 0;
        double usoInv        = 0;

        List<Map<String, Object>> productosResult = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            double cantidad = Math.max(0, result.get(i).doubleValue());
            cantidad = Math.round(cantidad * 100.0) / 100.0;

            double gananciaProd = Math.round(cantidad * ganancias[i] * 100.0) / 100.0;
            gananciaTotal += gananciaProd;

            usoCap  += cantidad;
            usoProv += cantidad * costos[i];
            usoCred += cantidad * costos[i] * 0.5;
            usoInv  += cantidad;

            Map<String, Object> prod = new LinkedHashMap<>();
            prod.put("nombre",        productos.get(i).getNombre());
            prod.put("cantidad",      cantidad);
            prod.put("ganancia",      gananciaProd);
            prod.put("precioVenta",   productos.get(i).getPrecio());
            prod.put("costoCompra",   costos[i]);
            prod.put("stockActual",   productos.get(i).getCantidad());
            productosResult.add(prod);
        }

        // Calcular % de uso de cada restricción (redondeado, máx 100%)
        Map<String, Double> usoRestricciones = new LinkedHashMap<>();
        usoRestricciones.put("capacidadVentas", pct(usoCap,  req.getCapacidadVentas()));
        usoRestricciones.put("presupuesto",     pct(usoProv, req.getPresupuesto()));
        usoRestricciones.put("credito",         pct(usoCred, req.getCreditoDisponible()));
        usoRestricciones.put("inventario",      pct(usoInv,  req.getInventarioTotal()));

        // Ordenar productos por ganancia descendente (el mejor primero)
        productosResult.sort((a, b) ->
            Double.compare((double) b.get("ganancia"), (double) a.get("ganancia")));

        OptimizacionResponse response = new OptimizacionResponse();
        response.setEstado("optimo");
        response.setGananciaOptima(Math.round(gananciaTotal * 100.0) / 100.0);
        response.setProductos(productosResult);
        response.setUsoRestricciones(usoRestricciones);
        return response;
    }

    /** Calcula el porcentaje de uso redondeado, con tope en 100% */
    private double pct(double usado, double total) {
        if (total <= 0) return 0;
        return Math.min(100, Math.round(usado / total * 100.0));
    }
}
