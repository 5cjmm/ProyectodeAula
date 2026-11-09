package com.ShopMaster.Mobile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.ShopMaster.Model.ProductoVendido;
import com.ShopMaster.Model.Productos;
import com.ShopMaster.Model.Venta;
import com.ShopMaster.Repository.ProductosRepository;
import com.ShopMaster.Repository.ProveedorRepository;
import com.ShopMaster.Repository.VentaRepository;

class MobileInventarioServiceTest {

    private static final String TIENDA = "T1";

    private ProductosRepository productosRepository;
    private VentaRepository ventaRepository;
    private ProveedorRepository proveedorRepository;
    private MobileInventarioService servicio;

    @BeforeEach
    void preparar() {
        productosRepository = mock(ProductosRepository.class);
        ventaRepository = mock(VentaRepository.class);
        proveedorRepository = mock(ProveedorRepository.class);
        // 2026-11-07 10:00 en Bogotá
        Clock reloj = Clock.fixed(Instant.parse("2026-11-07T15:00:00Z"), ZoneId.of("America/Bogota"));
        servicio = new MobileInventarioService(productosRepository, ventaRepository, proveedorRepository, reloj);

        List<Productos> lista = new ArrayList<>();
        lista.add(producto("P1", "ARR-1", "Arroz Diana", "Granos", 20, 4500));
        lista.add(producto("P2", "ACE-1", "Aceite Gourmet", "Aceites", 2, 8500));
        lista.add(producto("P3", "ACE-2", "Aceite Vegetal", "Aceites", 8, 8500));
        lista.add(producto("P4", "PAP-1", "Papel Higiénico", "Aseo", 0, 12000));
        lista.add(producto("P5", "AGU-1", "Agua Cristal", "Bebidas", 40, 1800));
        lista.add(producto("P6", "LEN-1", "Lentejas", "Granos", 30, 3000));
        lista.add(producto("P7", "GAL-1", "Galletas", null, 5, 2000));
        when(productosRepository.findByTiendaIdAndActivoTrue(TIENDA)).thenReturn(lista);
    }

    private static Productos producto(String id, String codigo, String nombre, String categoria, int cantidad, double precio) {
        Productos p = new Productos();
        p.setId(id);
        p.setCodigo(codigo);
        p.setNombre(nombre);
        p.setCategoria(categoria);
        p.setCantidad(cantidad);
        p.setPrecio(precio);
        p.setTiendaId(TIENDA);
        p.setActivo(true);
        return p;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> data(Map<String, Object> respuesta) {
        assertEquals(true, respuesta.get("success"));
        return (Map<String, Object>) respuesta.get("data");
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> lista(Map<String, Object> respuesta) {
        return (List<Map<String, Object>>) respuesta.get("data");
    }

    // ── Reglas de negocio ─────────────────────────────────────────────────────

    @Test
    void estadoSegunElStock() {
        assertEquals("AGOTADO", MobileInventarioService.estado(0));
        assertEquals("CRITICO", MobileInventarioService.estado(1));
        assertEquals("CRITICO", MobileInventarioService.estado(5));
        assertEquals("DISPONIBLE", MobileInventarioService.estado(6));
    }

    @Test
    void nivelStockSigueLaFormulaDelContrato() {
        assertEquals(0, MobileInventarioService.nivelStock(0));
        assertEquals(8, MobileInventarioService.nivelStock(2));
        assertEquals(80, MobileInventarioService.nivelStock(20));
        assertEquals(100, MobileInventarioService.nivelStock(25));
        assertEquals(100, MobileInventarioService.nivelStock(40));
    }

    // ── Resumen ───────────────────────────────────────────────────────────────

    @Test
    void resumenCuentaPorEstadoYCalculaElValor() {
        Map<String, Object> d = data(servicio.resumen(TIENDA));
        assertEquals(7, d.get("totalProductos"));
        assertEquals(4L, d.get("productosDisponibles")); // 20, 8, 40, 30
        assertEquals(2L, d.get("stockCritico"));          // 2 y 5
        assertEquals(1L, d.get("sinStock"));              // 0
        // 20*4500 + 2*8500 + 8*8500 + 0 + 40*1800 + 30*3000 + 5*2000
        assertEquals(20 * 4500L + 2 * 8500L + 8 * 8500L + 40 * 1800L + 30 * 3000L + 5 * 2000L, d.get("valorInventario"));
        assertNull(d.get("variacionValorMensual"));
    }

    // ── Lista de productos ────────────────────────────────────────────────────

    @Test
    void listarSinFiltrosOrdenaPorNombreYPagina() {
        Map<String, Object> r = servicio.listar(TIENDA, null, null, null, null, null, null);
        List<Map<String, Object>> items = lista(r);
        assertEquals(7, items.size());
        assertEquals("Aceite Gourmet", items.get(0).get("nombre"));
        assertEquals("Sin categoría", items.stream()
                .filter(m -> "P7".equals(m.get("id"))).findFirst().orElseThrow().get("categoria"));
        @SuppressWarnings("unchecked")
        Map<String, Object> pag = (Map<String, Object>) r.get("paginacion");
        assertEquals(0, pag.get("pagina"));
        assertEquals(20, pag.get("tamano"));
        assertEquals(7L, pag.get("totalElementos"));
        assertEquals(1L, pag.get("totalPaginas"));
    }

    @Test
    void listarFiltraPorEstadoCategoriaYTexto() {
        assertEquals(2, lista(servicio.listar(TIENDA, null, null, "CRITICO", null, null, null)).size());
        assertEquals(2, lista(servicio.listar(TIENDA, null, "Aceites", null, null, null, null)).size());
        assertEquals(1, lista(servicio.listar(TIENDA, null, "aceites", "CRITICO", null, null, null)).size());
        assertEquals(1, lista(servicio.listar(TIENDA, "arr-1", null, null, null, null, null)).size()); // por código
        assertEquals(2, lista(servicio.listar(TIENDA, "ACEITE", null, null, null, null, null)).size()); // por nombre
        assertEquals(0, lista(servicio.listar(TIENDA, "zzz", null, null, null, null, null)).size());
    }

    @Test
    void listarOrdenaPorStockYPrecio() {
        List<Map<String, Object>> asc = lista(servicio.listar(TIENDA, null, null, null, "stock_asc", null, null));
        assertEquals("P4", asc.get(0).get("id"));
        assertEquals("P5", asc.get(asc.size() - 1).get("id"));

        List<Map<String, Object>> precioDesc = lista(servicio.listar(TIENDA, null, null, null, "precio_desc", null, null));
        assertEquals("P4", precioDesc.get(0).get("id"));
    }

    @Test
    void listarPaginaLosResultados() {
        Map<String, Object> r = servicio.listar(TIENDA, null, null, null, "stock_asc", "1", "3");
        assertEquals(3, lista(r).size());
        @SuppressWarnings("unchecked")
        Map<String, Object> pag = (Map<String, Object>) r.get("paginacion");
        assertEquals(3L, pag.get("totalPaginas")); // 7 productos de a 3
        assertEquals(1, pag.get("pagina"));

        assertEquals(1, lista(servicio.listar(TIENDA, null, null, null, "stock_asc", "2", "3")).size());
        assertEquals(0, lista(servicio.listar(TIENDA, null, null, null, "stock_asc", "9", "3")).size());
    }

    @Test
    void listarRechazaParametrosInvalidosConError400() {
        MobileException e = assertThrows(MobileException.class,
                () -> servicio.listar(TIENDA, null, null, "XYZ", null, null, null));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("El parámetro estado debe ser DISPONIBLE, CRITICO o AGOTADO", e.getMessage());

        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(MobileException.class,
                () -> servicio.listar(TIENDA, null, null, null, "color", null, null)).getStatus());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(MobileException.class,
                () -> servicio.listar(TIENDA, null, null, null, null, "-1", null)).getStatus());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(MobileException.class,
                () -> servicio.listar(TIENDA, null, null, null, null, null, "0")).getStatus());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(MobileException.class,
                () -> servicio.listar(TIENDA, null, null, null, null, null, "101")).getStatus());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(MobileException.class,
                () -> servicio.listar(TIENDA, null, null, null, null, "abc", null)).getStatus());
    }

    // ── Detalle ───────────────────────────────────────────────────────────────

    @Test
    void detalleDevuelveLosCamposYLasVentasDeSieteDias() {
        Productos arroz = producto("P1", "ARR-1", "Arroz Diana", "Granos", 20, 4500);
        when(productosRepository.findById("P1")).thenReturn(Optional.of(arroz));

        // Ventas: 5 unidades hoy (7 nov) y 3 el 1 nov; otra venta de otro producto no cuenta.
        Venta hoy = venta("2026-11-07T14:00:00Z", pv("P1", "ARR-1", 5), pv("P5", "AGU-1", 9));
        Venta antes = venta("2026-11-01T20:00:00Z", pv("P1", "ARR-1", 3));
        when(ventaRepository.findByTiendaIdAndFechaBetweenAndActivoTrue(eq(TIENDA), any(Date.class), any(Date.class)))
                .thenReturn(List.of(hoy, antes));

        Map<String, Object> d = data(servicio.detalle(TIENDA, "P1"));
        assertEquals("ARR-1", d.get("codigo"));
        assertEquals(4500L, d.get("precio"));
        assertEquals(80, d.get("nivelStock"));
        assertEquals("DISPONIBLE", d.get("estado"));
        assertEquals(5, d.get("stockMinimo"));
        assertNull(d.get("ubicacion"));
        assertNull(d.get("proveedor"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> ventas = (List<Map<String, Object>>) d.get("ventasUltimos7Dias");
        assertEquals(7, ventas.size());
        assertEquals("2026-11-01", ventas.get(0).get("fecha"));
        assertEquals(3, ventas.get(0).get("unidades"));
        assertEquals("2026-11-07", ventas.get(6).get("fecha"));
        assertEquals(5, ventas.get(6).get("unidades"));
        assertEquals(0, ventas.get(3).get("unidades"));
    }

    @Test
    void detalleDeUnProductoDeOtraTiendaOInactivoEs404() {
        Productos ajeno = producto("PX", "X", "Ajeno", "Aseo", 10, 1000);
        ajeno.setTiendaId("OTRA");
        when(productosRepository.findById("PX")).thenReturn(Optional.of(ajeno));
        Productos borrado = producto("PB", "B", "Borrado", "Aseo", 10, 1000);
        borrado.setActivo(false);
        when(productosRepository.findById("PB")).thenReturn(Optional.of(borrado));
        when(productosRepository.findById("P999")).thenReturn(Optional.empty());

        for (String id : List.of("PX", "PB", "P999")) {
            MobileException e = assertThrows(MobileException.class, () -> servicio.detalle(TIENDA, id));
            assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
            assertEquals("Producto no encontrado", e.getMessage());
        }
    }

    private static ProductoVendido pv(String productoId, String codigo, int cantidad) {
        ProductoVendido p = new ProductoVendido();
        p.setProductoId(productoId);
        p.setCodigo(codigo);
        p.setCantidad(cantidad);
        return p;
    }

    private static Venta venta(String fechaUtc, ProductoVendido... productos) {
        Venta v = new Venta();
        v.setFecha(Date.from(Instant.parse(fechaUtc)));
        v.setProductos(List.of(productos));
        return v;
    }

    // ── Stock crítico ─────────────────────────────────────────────────────────

    @Test
    void stockCriticoTraeAgotadosPrimeroYLosContadoresCompletos() {
        Map<String, Object> d = data(servicio.stockCritico(TIENDA, null, null));
        assertEquals(2L, d.get("totalCritico"));
        assertEquals(1L, d.get("totalAgotado"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> productos = (List<Map<String, Object>>) d.get("productos");
        assertEquals(3, productos.size());
        assertEquals("AGOTADO", productos.get(0).get("estado"));
        assertEquals(0, productos.get(0).get("stock"));
        assertEquals(2, productos.get(1).get("stock"));
        assertEquals(5, productos.get(2).get("stock"));
    }

    @Test
    void stockCriticoAplicaEstadoYLimitePeroNoCambiaLosTotales() {
        Map<String, Object> d = data(servicio.stockCritico(TIENDA, "CRITICO", "1"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> productos = (List<Map<String, Object>>) d.get("productos");
        assertEquals(1, productos.size());
        assertEquals("CRITICO", productos.get(0).get("estado"));
        assertEquals(2L, d.get("totalCritico"));
        assertEquals(1L, d.get("totalAgotado"));
    }

    @Test
    void stockCriticoRechazaEstadoInvalido() {
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(MobileException.class,
                () -> servicio.stockCritico(TIENDA, "DISPONIBLE", null)).getStatus());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(MobileException.class,
                () -> servicio.stockCritico(TIENDA, null, "0")).getStatus());
    }

    // ── Estadísticas ──────────────────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void estadisticasAgrupaPorCategoria() {
        Map<String, Object> d = data(servicio.estadisticas(TIENDA, null));
        assertEquals(7, d.get("totalProductos"));

        List<Map<String, Object>> categorias = (List<Map<String, Object>>) d.get("categorias");
        assertEquals(5, categorias.size()); // Aceites 2, Granos 2, Aseo 1, Bebidas 1, Sin categoría 1
        int suma = categorias.stream().mapToInt(c -> (Integer) c.get("cantidad")).sum();
        assertEquals(7, suma);
        assertEquals(2, categorias.get(0).get("cantidad"));

        Map<String, Object> niveles = (Map<String, Object>) d.get("nivelesStock");
        assertEquals(4L, niveles.get("disponible"));
        assertEquals(2L, niveles.get("critico"));
        assertEquals(1L, niveles.get("agotado"));

        List<Map<String, Object>> mas = (List<Map<String, Object>>) d.get("productosMasStock");
        assertEquals(4, mas.size());
        assertEquals("P5", mas.get(0).get("id")); // 40
        assertEquals("P6", mas.get(1).get("id")); // 30
        assertEquals("P1", mas.get(2).get("id")); // 20

        List<Map<String, Object>> cobertura = (List<Map<String, Object>>) d.get("coberturaCategorias");
        Map<String, Object> aceites = cobertura.stream()
                .filter(c -> "Aceites".equals(c.get("categoria"))).findFirst().orElseThrow();
        // niveles: 2 → 8 %, 8 → 32 %; promedio 20 %
        assertEquals(20L, aceites.get("porcentaje"));
        assertNull(d.get("valorUltimosDias"));
    }

    @Test
    void estadisticasValidaElParametroDias() {
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(MobileException.class,
                () -> servicio.estadisticas(TIENDA, "0")).getStatus());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(MobileException.class,
                () -> servicio.estadisticas(TIENDA, "91")).getStatus());
        data(servicio.estadisticas(TIENDA, "30"));
    }
}
