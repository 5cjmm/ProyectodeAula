package com.ShopMaster.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import com.ShopMaster.Model.Productos;
import com.ShopMaster.Repository.ProductosRepository;
import com.ShopMaster.Repository.ProveedorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProductosServiceTest {

    @Mock
    private ProductosRepository productosRepository;

    @Mock
    private ProveedorRepository proveedorRepository;

    @InjectMocks
    private ProductosService productosService;

    @Test
    void guardarProducto_creaCuandoNoExiste() {
        Productos p = new Productos();
        p.setCodigo("ABC123");
        p.setTiendaId("t1");
        p.setCantidad(10);
        p.setPrecio(5.0);

        when(productosRepository.existsByCodigoAndTiendaId("ABC123", "t1")).thenReturn(false);
        when(productosRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Productos out = productosService.guardarProducto(p, "t1");

        verify(productosRepository).save(p);
        assertEquals(10, out.getCantidad());
        assertEquals(5.0, out.getPrecio());
    }

    @Test
    void guardarProducto_codigoDuplicado_lanzaExcepcion() {
        Productos p = new Productos();
        p.setCodigo("X");
        p.setTiendaId("t1");

        when(productosRepository.existsByCodigoAndTiendaId("X", "t1")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> productosService.guardarProducto(p, "t1"));
        verify(productosRepository, never()).save(any());
    }

    @Test
    void actualizarProducto_codigoDuplicado_lanzaExcepcion() {
        Productos existente = new Productos();
        existente.setId("p1");
        existente.setTiendaId("t1");

        Productos nuevo = new Productos();
        nuevo.setCodigo("NEW");

        when(productosRepository.findById("p1")).thenReturn(Optional.of(existente));
        when(productosRepository.existsByCodigoAndTiendaIdAndIdNot("NEW", "t1", "p1")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> productosService.actualizarProducto("p1", nuevo));
    }

    @Test
    void guardarProducto_negativePriceOrQuantity_isAllowed_byService() {
        // Nota: el servicio actual no valida precio/cantidad, este test documenta el comportamiento
        Productos p = new Productos();
        p.setCodigo("N1");
        p.setTiendaId("t1");
        p.setCantidad(-5);
        p.setPrecio(-10.0);

        when(productosRepository.existsByCodigoAndTiendaId("N1", "t1")).thenReturn(false);
        when(productosRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Productos out = productosService.guardarProducto(p, "t1");

        assertEquals(-5, out.getCantidad());
        assertEquals(-10.0, out.getPrecio());
    }
}
