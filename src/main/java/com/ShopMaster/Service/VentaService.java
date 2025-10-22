package com.ShopMaster.Service;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ShopMaster.Model.ProductoVendido;
import com.ShopMaster.Model.Productos;
import com.ShopMaster.Model.Venta;
import com.ShopMaster.Repository.ProductosRepository;
import com.ShopMaster.Repository.VentaRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ProductosRepository productosRepository;

    public List<Venta> obtenerTodaslasVentas() {
        return ventaRepository.findAll();
    }

    public void eliminarVenta(String id) {
        ventaRepository.deleteById(id);
    }

   

    public Venta guardarVenta(Venta venta) {
        return ventaRepository.save(venta);
    }

    public Page<Venta> obtenerVentasPorTienda(String tiendaId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ventaRepository.findByTiendaId(tiendaId, pageable);
    }
}

