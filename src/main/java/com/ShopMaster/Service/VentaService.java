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

    public Venta registrarVenta(Venta venta) {
        double total = 0;

        for (ProductoVendido pv : venta.getProductos()) {
            Productos producto = productosRepository.findById(pv.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            if (pv.getCantidad() > producto.getCantidad()) {
                throw new RuntimeException("Cantidad insuficiente para el producto: " + producto.getNombre());
            }

            producto.setCantidad(producto.getCantidad() - pv.getCantidad());
            productosRepository.save(producto);

            pv.setSubtotal(pv.getCantidad() * pv.getPrecioUnitario());
            total += pv.getSubtotal();
        }

        venta.setFecha(new Date());
        venta.setTotal(total);

        return ventaRepository.save(venta);
    }

    public Page<Venta> obtenerVentasPorTienda(String tiendaId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ventaRepository.findByTiendaId(tiendaId, pageable);
    }
}

