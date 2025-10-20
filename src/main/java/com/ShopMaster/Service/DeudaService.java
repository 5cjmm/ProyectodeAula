package com.ShopMaster.Service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ShopMaster.Model.Abono;
import com.ShopMaster.Model.Deuda;
import com.ShopMaster.Model.ProductoVendido;
import com.ShopMaster.Model.Productos;
import com.ShopMaster.Repository.DeudaRepository;
import com.ShopMaster.Repository.ProductosRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeudaService {

    private final DeudaRepository deudaRepository;
    private final ProductosRepository productosRepository;

    public Deuda registrarDeuda(Deuda deuda) {
        double total = 0;

        for (ProductoVendido pv : deuda.getProductos()) {
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

        deuda.setTotal(total);
        deuda.setTotalRestante(total);
        deuda.setEstado("NO PAGADA");
        deuda.setFechaVenta(LocalDateTime.now());

        return deudaRepository.save(deuda);
    }

    public Page<Deuda> obtenerDeudasPorTienda(String tiendaId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return deudaRepository.findByTiendaId(tiendaId, pageable);
    }

    public Deuda registrarAbono(String deudaId, Abono abono) {
        Deuda deuda = deudaRepository.findById(deudaId)
                .orElseThrow(() -> new RuntimeException("Deuda no encontrada"));

        deuda.getHistorialAbonos().add(abono);
        deuda.setTotalRestante(deuda.getTotalRestante() - abono.getMonto());

        if (deuda.getTotalRestante() <= 0) {
            deuda.setEstado("PAGADA");
            deuda.setTotalRestante(0);
        } else if (deuda.getTotalRestante() < deuda.getTotal()) {
            deuda.setEstado("PARCIAL");
        }

        return deudaRepository.save(deuda);
    }

    public void eliminarDeuda(String id) {
        deudaRepository.deleteById(id);
    }
}

