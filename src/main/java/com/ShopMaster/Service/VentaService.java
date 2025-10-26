package com.ShopMaster.Service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ShopMaster.Model.Venta;
import com.ShopMaster.Repository.VentaRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class VentaService {

    private final VentaRepository ventaRepository;

    public List<Venta> obtenerTodaslasVentas() {
        return ventaRepository.findAll();
    }

    public void eliminarVenta(String id) {
        ventaRepository.deleteById(id);
    }

    // Método utilizado por el controlador: registrarVenta
    public Venta registrarVenta(Venta venta) {
        // Si necesita lógica adicional (actualizar stock, calcular montos), añadir aquí.
        // Guardamos la venta tal cual en el repositorio.
        return ventaRepository.save(venta);
    }

    // Método con nombre alternativo (si otras partes usan guardarVenta)
    public Venta guardarVenta(Venta venta) {
        return registrarVenta(venta);
    }

    public Page<Venta> obtenerVentasPorTienda(String tiendaId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ventaRepository.findByTiendaId(tiendaId, pageable);
    }
}

