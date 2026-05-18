package com.ShopMaster.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import com.ShopMaster.Model.ProductoVendido;
import com.ShopMaster.Model.Productos;
import com.ShopMaster.Model.Venta;
import com.ShopMaster.Repository.ProductosRepository;
import com.ShopMaster.Repository.UsuarioRepository;
import com.ShopMaster.Repository.VentaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

@ExtendWith(MockitoExtension.class)
public class VentaServiceTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ProductosRepository productosRepository;

    @InjectMocks
    private VentaService ventaService;

    @Test
    void registrarVenta_calculaTotalYSubtotales_yDescuentaStock_yGuardaVenta() {
        Productos prod = new Productos();
        prod.setId("prod-1");
        prod.setNombre("P1");
        prod.setCantidad(10);
        prod.setPrecio(2.0);

        ProductoVendido pv = new ProductoVendido();
        pv.setProductoId("prod-1");
        pv.setCantidad(3);
        pv.setPrecioUnitario(2.0);

        Venta v = new Venta();
        v.setProductos(List.of(pv));
        v.setTotal(0);

        when(productosRepository.findById("prod-1")).thenReturn(Optional.of(prod));
        when(productosRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(ventaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Venta out = ventaService.registrarVenta(v);

        // subtotal calculado y asignado
        assertEquals(6.0, pv.getSubtotal());
        // total calculado
        assertEquals(6.0, out.getTotal());
        // stock reducido
        verify(productosRepository).save(argThat(p -> p.getId().equals("prod-1") && p.getCantidad() == 7));
        // venta guardada
        verify(ventaRepository).save(any(Venta.class));
    }

    @Test
    void registrarVenta_venderMasQueStock_lanzaExcepcion() {
        Productos prod = new Productos();
        prod.setId("p2");
        prod.setNombre("P2");
        prod.setCantidad(2);

        ProductoVendido pv = new ProductoVendido();
        pv.setProductoId("p2");
        pv.setCantidad(5);
        pv.setPrecioUnitario(1.0);

        Venta v = new Venta();
        v.setProductos(List.of(pv));

        when(productosRepository.findById("p2")).thenReturn(Optional.of(prod));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> ventaService.registrarVenta(v));
        assertTrue(ex.getMessage().toLowerCase().contains("cantidad insuficiente") || ex.getMessage().toLowerCase().contains("insuficiente"));
        verify(ventaRepository, never()).save(any());
    }
}
