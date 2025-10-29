package com.ShopMaster.Service;

import java.time.LocalDateTime;
import java.util.Optional;

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
Optional<Deuda> deudaExistenteOpt = deudaRepository.findByCedulaCliente(deuda.getCedulaCliente());

    if (deudaExistenteOpt.isPresent()) {
        Deuda deudaExistente = deudaExistenteOpt.get();

        if (!deudaExistente.getNombreCliente().equalsIgnoreCase(deuda.getNombreCliente())) {
            throw new RuntimeException("La cédula ya está registrada con otro cliente");
        }

        double totalNuevo = 0;
        for (ProductoVendido pv : deuda.getProductos()) {
            Productos producto = productosRepository.findById(pv.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            if (pv.getCantidad() > producto.getCantidad()) {
                throw new RuntimeException("Cantidad insuficiente para el producto: " + producto.getNombre());
            }

            producto.setCantidad(producto.getCantidad() - pv.getCantidad());
            productosRepository.save(producto);

            pv.setSubtotal(pv.getCantidad() * pv.getPrecioUnitario());
            totalNuevo += pv.getSubtotal();
        }

        deudaExistente.getProductos().addAll(deuda.getProductos());
        deudaExistente.setTotal(deudaExistente.getTotal() + totalNuevo);
        deudaExistente.setTotalRestante(deudaExistente.getTotalRestante() + totalNuevo);
        deudaExistente.setEstado("NO PAGADA");
        deudaExistente.setFechaVenta(LocalDateTime.now());

        deudaRepository.save(deudaExistente);

        throw new RuntimeException("ACTUALIZADA");
    }

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

    // Nuevo: obtener todas las deudas para la vista
    public java.util.List<Deuda> obtenerTodasLasDeudas() {
        return deudaRepository.findAll();
    }

    // Nuevo: obtener deuda por id (devuelve Optional para que el controlador lo maneje)
    public java.util.Optional<Deuda> obtenerDeudaPorId(String id) {
        return deudaRepository.findById(id);
    }

    public Deuda registrarAbono(String deudaId, Abono abono) {
        Deuda deuda = deudaRepository.findById(deudaId)
                .orElseThrow(() -> new RuntimeException("Deuda no encontrada"));

        if (abono.getFecha() == null) {
            abono.setFecha(LocalDateTime.now());
        }

        if (abono.getMonto() > deuda.getTotalRestante()) {
            throw new RuntimeException("El monto del abono no puede superar la deuda restante");
        }

                
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

    // Sobrecarga usada por el controlador que pasa sólo un monto (double)
    public Deuda registrarAbono(String deudaId, double monto) {
        Abono abono = new Abono();
        abono.setMonto(monto);
        abono.setFecha(LocalDateTime.now());
        return registrarAbono(deudaId, abono);
    }

    public void eliminarDeuda(String id) {
        deudaRepository.deleteById(id);
    }
}

