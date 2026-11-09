package com.ShopMaster.Mobile;

import java.text.Collator;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ShopMaster.Model.ProductoVendido;
import com.ShopMaster.Model.Productos;
import com.ShopMaster.Model.Venta;
import com.ShopMaster.Repository.ProductosRepository;
import com.ShopMaster.Repository.ProveedorRepository;
import com.ShopMaster.Repository.VentaRepository;

/**
 * Reglas de inventario de la API móvil (contrato v1.1/v1.2). Todas las consultas se
 * hacen sobre los productos ACTIVOS de la tienda del usuario autenticado.
 */
@Service
public class MobileInventarioService {

    public static final int STOCK_MINIMO = 5;
    public static final String SIN_CATEGORIA = "Sin categoría";

    private static final Set<String> ESTADOS = Set.of("DISPONIBLE", "CRITICO", "AGOTADO");
    private static final Set<String> ORDENES = Set.of("nombre", "stock_asc", "stock_desc", "precio_asc", "precio_desc");

    private final ProductosRepository productosRepository;
    private final VentaRepository ventaRepository;
    private final ProveedorRepository proveedorRepository;
    private final Clock clock;

    @Autowired
    public MobileInventarioService(ProductosRepository productosRepository,
                                   VentaRepository ventaRepository,
                                   ProveedorRepository proveedorRepository) {
        this(productosRepository, ventaRepository, proveedorRepository,
                Clock.system(ZoneId.of("America/Bogota")));
    }

    MobileInventarioService(ProductosRepository productosRepository,
                            VentaRepository ventaRepository,
                            ProveedorRepository proveedorRepository,
                            Clock clock) {
        this.productosRepository = productosRepository;
        this.ventaRepository = ventaRepository;
        this.proveedorRepository = proveedorRepository;
        this.clock = clock;
    }

    // ── Reglas de negocio (sección 2.1 del contrato) ──────────────────────────

    public static String estado(int stock) {
        if (stock <= 0) {
            return "AGOTADO";
        }
        return stock <= STOCK_MINIMO ? "CRITICO" : "DISPONIBLE";
    }

    /** min(100, redondeo(100 × stock ÷ (stockMinimo × 5))). */
    public static int nivelStock(int stock) {
        long nivel = Math.round(100.0 * Math.max(0, stock) / (STOCK_MINIMO * 5.0));
        return (int) Math.min(100, nivel);
    }

    static String categoria(Productos p) {
        String c = p.getCategoria();
        return (c == null || c.isBlank()) ? SIN_CATEGORIA : c.trim();
    }

    /** Enteros sin decimales cuando el número es entero (4500 y no 4500.0). */
    static Number numero(double valor) {
        if (valor == Math.rint(valor) && Math.abs(valor) < 1e15) {
            return (long) valor;
        }
        return valor;
    }

    private List<Productos> activos(String tiendaId) {
        List<Productos> lista = productosRepository.findByTiendaIdAndActivoTrue(tiendaId);
        return lista == null ? List.of() : lista;
    }

    // ── GET /api/inventario/resumen ───────────────────────────────────────────

    public Map<String, Object> resumen(String tiendaId) {
        List<Productos> lista = activos(tiendaId);
        long disponibles = lista.stream().filter(p -> "DISPONIBLE".equals(estado(p.getCantidad()))).count();
        long criticos = lista.stream().filter(p -> "CRITICO".equals(estado(p.getCantidad()))).count();
        long agotados = lista.stream().filter(p -> "AGOTADO".equals(estado(p.getCantidad()))).count();

        Map<String, Object> d = new LinkedHashMap<>();
        d.put("totalProductos", lista.size());
        d.put("productosDisponibles", disponibles);
        d.put("stockCritico", criticos);
        d.put("sinStock", agotados);
        d.put("valorInventario", numero(valorInventario(lista)));
        // Opcional: no se guarda historial del valor del inventario, la app oculta la flecha.
        d.put("variacionValorMensual", null);
        return MobileApiResponse.ok(d);
    }

    private static double valorInventario(List<Productos> lista) {
        return lista.stream().mapToDouble(p -> p.getPrecio() * Math.max(0, p.getCantidad())).sum();
    }

    // ── GET /api/productos ────────────────────────────────────────────────────

    public Map<String, Object> listar(String tiendaId, String q, String categoria, String estado,
                                      String orden, String pagina, String tamano) {
        String estadoFiltro = null;
        if (estado != null && !estado.isBlank()) {
            estadoFiltro = estado.trim().toUpperCase(Locale.ROOT);
            if (!ESTADOS.contains(estadoFiltro)) {
                throw MobileException.badRequest("El parámetro estado debe ser DISPONIBLE, CRITICO o AGOTADO");
            }
        }
        String ordenFiltro = (orden == null || orden.isBlank()) ? "nombre" : orden.trim();
        if (!ORDENES.contains(ordenFiltro)) {
            throw MobileException.badRequest(
                    "El parámetro orden debe ser nombre, stock_asc, stock_desc, precio_asc o precio_desc");
        }
        int pag = entero("pagina", pagina, 0, Integer.MAX_VALUE, 0);
        int tam = entero("tamano", tamano, 1, 100, 20);

        final String estadoFinal = estadoFiltro;
        final String texto = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        final String cat = (categoria == null || categoria.isBlank()) ? null : categoria.trim();

        List<Productos> filtrados = activos(tiendaId).stream()
                .filter(p -> texto.isEmpty()
                        || contiene(p.getNombre(), texto)
                        || contiene(p.getCodigo(), texto))
                .filter(p -> cat == null || categoria(p).equalsIgnoreCase(cat))
                .filter(p -> estadoFinal == null || estadoFinal.equals(estado(p.getCantidad())))
                .sorted(comparador(ordenFiltro))
                .collect(Collectors.toList());

        long total = filtrados.size();
        long totalPaginas = (total + tam - 1) / tam;
        long desde = (long) pag * tam;
        List<Map<String, Object>> pagina0 = new ArrayList<>();
        for (long i = desde; i < Math.min(total, desde + tam); i++) {
            pagina0.add(itemLista(filtrados.get((int) i)));
        }

        Map<String, Object> paginacion = new LinkedHashMap<>();
        paginacion.put("pagina", pag);
        paginacion.put("tamano", tam);
        paginacion.put("totalElementos", total);
        paginacion.put("totalPaginas", totalPaginas);
        return MobileApiResponse.ok(pagina0, paginacion);
    }

    private static boolean contiene(String campo, String textoEnMinuscula) {
        return campo != null && campo.toLowerCase(Locale.ROOT).contains(textoEnMinuscula);
    }

    private static Comparator<Productos> comparador(String orden) {
        Collator collator = Collator.getInstance(Locale.of("es", "CO"));
        Comparator<Productos> porNombre = Comparator.comparing(
                (Productos p) -> p.getNombre() == null ? "" : p.getNombre(), collator);
        switch (orden) {
            case "stock_asc":
                return Comparator.comparingInt(Productos::getCantidad).thenComparing(porNombre);
            case "stock_desc":
                return Comparator.comparingInt(Productos::getCantidad).reversed().thenComparing(porNombre);
            case "precio_asc":
                return Comparator.comparingDouble(Productos::getPrecio).thenComparing(porNombre);
            case "precio_desc":
                return Comparator.comparingDouble(Productos::getPrecio).reversed().thenComparing(porNombre);
            default:
                return porNombre;
        }
    }

    private static Map<String, Object> itemLista(Productos p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("nombre", p.getNombre());
        m.put("categoria", categoria(p));
        m.put("precio", numero(p.getPrecio()));
        m.put("stock", p.getCantidad());
        m.put("stockMinimo", STOCK_MINIMO);
        m.put("estado", estado(p.getCantidad()));
        return m;
    }

    // ── GET /api/productos/{id} ───────────────────────────────────────────────

    public Map<String, Object> detalle(String tiendaId, String id) {
        Productos p = productosRepository.findById(id)
                .filter(x -> x.isActivo() && tiendaId.equals(x.getTiendaId()))
                .orElseThrow(() -> MobileException.notFound("Producto no encontrado"));

        Map<String, Object> d = new LinkedHashMap<>();
        d.put("id", p.getId());
        d.put("codigo", p.getCodigo());
        d.put("codigoBarras", null);   // opcional: aún no se registra
        d.put("nombre", p.getNombre());
        d.put("descripcion", null);    // opcional: aún no se registra
        d.put("categoria", categoria(p));
        d.put("precio", numero(p.getPrecio()));
        d.put("stock", p.getCantidad());
        d.put("stockMinimo", STOCK_MINIMO);
        d.put("nivelStock", nivelStock(p.getCantidad()));
        d.put("estado", estado(p.getCantidad()));
        d.put("presentacion", null);   // opcional: aún no se registra
        d.put("proveedor", nombreProveedor(p));
        d.put("ubicacion", null);      // opcional: aún no se registra
        d.put("ventasUltimos7Dias", ventasUltimos7Dias(tiendaId, p));
        return MobileApiResponse.ok(d);
    }

    private String nombreProveedor(Productos p) {
        if (p.getProveedorIds() == null) {
            return null;
        }
        for (ObjectId oid : p.getProveedorIds()) {
            if (oid == null) {
                continue;
            }
            var proveedor = proveedorRepository.findById(oid.toHexString());
            if (proveedor.isPresent() && proveedor.get().isActivo()) {
                return proveedor.get().getNombre();
            }
        }
        return null;
    }

    /** Siete días terminando hoy, del más antiguo al más reciente; los días sin ventas valen 0. */
    private List<Map<String, Object>> ventasUltimos7Dias(String tiendaId, Productos p) {
        ZoneId zona = clock.getZone();
        LocalDate hoy = LocalDate.now(clock);
        LocalDate primero = hoy.minusDays(6);
        Date desde = Date.from(primero.atStartOfDay(zona).toInstant());
        Date hasta = Date.from(hoy.plusDays(1).atStartOfDay(zona).toInstant());

        Map<LocalDate, Integer> porDia = new TreeMap<>();
        for (int i = 0; i < 7; i++) {
            porDia.put(primero.plusDays(i), 0);
        }

        List<Venta> ventas = ventaRepository.findByTiendaIdAndFechaBetweenAndActivoTrue(tiendaId, desde, hasta);
        if (ventas != null) {
            for (Venta v : ventas) {
                if (v.getFecha() == null || v.getProductos() == null) {
                    continue;
                }
                LocalDate dia = v.getFecha().toInstant().atZone(zona).toLocalDate();
                if (!porDia.containsKey(dia)) {
                    continue;
                }
                for (ProductoVendido pv : v.getProductos()) {
                    if (esMismoProducto(pv, p)) {
                        porDia.merge(dia, pv.getCantidad(), Integer::sum);
                    }
                }
            }
        }

        List<Map<String, Object>> salida = new ArrayList<>();
        porDia.forEach((dia, unidades) -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("fecha", dia.toString());
            m.put("unidades", unidades);
            salida.add(m);
        });
        return salida;
    }

    private static boolean esMismoProducto(ProductoVendido pv, Productos p) {
        if (pv.getProductoId() != null && pv.getProductoId().equals(p.getId())) {
            return true;
        }
        return pv.getProductoId() == null && pv.getCodigo() != null && pv.getCodigo().equals(p.getCodigo());
    }

    // ── GET /api/productos/stock-critico ──────────────────────────────────────

    public Map<String, Object> stockCritico(String tiendaId, String estado, String limite) {
        String estadoFiltro = null;
        if (estado != null && !estado.isBlank()) {
            estadoFiltro = estado.trim().toUpperCase(Locale.ROOT);
            if (!"CRITICO".equals(estadoFiltro) && !"AGOTADO".equals(estadoFiltro)) {
                throw MobileException.badRequest("El parámetro estado debe ser CRITICO o AGOTADO");
            }
        }
        int max = entero("limite", limite, 1, Integer.MAX_VALUE, Integer.MAX_VALUE);

        List<Productos> lista = activos(tiendaId);
        long totalCritico = lista.stream().filter(p -> "CRITICO".equals(estado(p.getCantidad()))).count();
        long totalAgotado = lista.stream().filter(p -> "AGOTADO".equals(estado(p.getCantidad()))).count();

        final String filtro = estadoFiltro;
        Comparator<Productos> orden = comparador("stock_asc");
        List<Map<String, Object>> productos = lista.stream()
                .filter(p -> !"DISPONIBLE".equals(estado(p.getCantidad())))
                .filter(p -> filtro == null || filtro.equals(estado(p.getCantidad())))
                .sorted(orden)
                .limit(max)
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", p.getId());
                    m.put("nombre", p.getNombre());
                    m.put("stock", p.getCantidad());
                    m.put("stockMinimo", STOCK_MINIMO);
                    m.put("estado", estado(p.getCantidad()));
                    return m;
                })
                .collect(Collectors.toList());

        Map<String, Object> d = new LinkedHashMap<>();
        d.put("totalCritico", totalCritico);
        d.put("totalAgotado", totalAgotado);
        d.put("productos", productos);
        return MobileApiResponse.ok(d);
    }

    // ── GET /api/inventario/estadisticas ──────────────────────────────────────

    public Map<String, Object> estadisticas(String tiendaId, String dias) {
        entero("dias", dias, 1, 90, 7); // solo se valida: aún no hay historial de valor

        List<Productos> lista = activos(tiendaId);

        Map<String, List<Productos>> porCategoria = new LinkedHashMap<>();
        for (Productos p : lista) {
            porCategoria.computeIfAbsent(categoria(p), k -> new ArrayList<>()).add(p);
        }
        List<Map.Entry<String, List<Productos>>> categoriasOrdenadas = porCategoria.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, List<Productos>>>comparingInt(e -> e.getValue().size())
                        .reversed()
                        .thenComparing(Map.Entry::getKey))
                .collect(Collectors.toList());

        List<Map<String, Object>> categorias = new ArrayList<>();
        List<Map<String, Object>> cobertura = new ArrayList<>();
        for (var e : categoriasOrdenadas) {
            Map<String, Object> c = new LinkedHashMap<>();
            c.put("nombre", e.getKey());
            c.put("cantidad", e.getValue().size());
            categorias.add(c);

            double promedio = e.getValue().stream().mapToInt(p -> nivelStock(p.getCantidad())).average().orElse(0);
            Map<String, Object> cb = new LinkedHashMap<>();
            cb.put("categoria", e.getKey());
            cb.put("porcentaje", Math.round(promedio));
            cobertura.add(cb);
        }

        Map<String, Object> niveles = new LinkedHashMap<>();
        niveles.put("disponible", lista.stream().filter(p -> "DISPONIBLE".equals(estado(p.getCantidad()))).count());
        niveles.put("critico", lista.stream().filter(p -> "CRITICO".equals(estado(p.getCantidad()))).count());
        niveles.put("agotado", lista.stream().filter(p -> "AGOTADO".equals(estado(p.getCantidad()))).count());

        List<Map<String, Object>> masStock = lista.stream()
                .sorted(Comparator.comparingInt(Productos::getCantidad).reversed()
                        .thenComparing(p -> p.getNombre() == null ? "" : p.getNombre()))
                .limit(4)
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", p.getId());
                    m.put("nombre", p.getNombre());
                    m.put("stock", p.getCantidad());
                    return m;
                })
                .collect(Collectors.toList());

        Map<String, Object> d = new LinkedHashMap<>();
        d.put("totalProductos", lista.size());
        d.put("valorInventario", numero(valorInventario(lista)));
        d.put("categorias", categorias);
        d.put("nivelesStock", niveles);
        // Opcional: sin historial guardado viene null y la app oculta la línea.
        d.put("valorUltimosDias", null);
        d.put("productosMasStock", masStock);
        d.put("coberturaCategorias", cobertura);
        return MobileApiResponse.ok(d);
    }

    // ── Utilidades ────────────────────────────────────────────────────────────

    /** Lee un entero opcional; si no es número o queda fuera de rango responde 400. */
    static int entero(String nombre, String valor, int min, int max, int porDefecto) {
        if (valor == null || valor.isBlank()) {
            return porDefecto;
        }
        try {
            int n = Integer.parseInt(valor.trim());
            if (n < min || n > max) {
                throw new NumberFormatException();
            }
            return n;
        } catch (NumberFormatException e) {
            String rango = max == Integer.MAX_VALUE ? "mayor o igual a " + min : "entre " + min + " y " + max;
            throw MobileException.badRequest("El parámetro " + nombre + " debe ser un número " + rango);
        }
    }
}
