package com.ShopMaster.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ShopMaster.Model.Venta;
import com.ShopMaster.Repository.VentaRepository;

@Service
public class VentaService {
    
    @Autowired
    private VentaRepository ventaRepository;

    public List<Venta> obtenerTodaslasVentas() {
        return ventaRepository.findAll();
    }

    public void eliminarVenta(String id) {
        ventaRepository.deleteById(id);
    }

   

    public Venta guardarVenta(Venta venta) {
        return ventaRepository.save(venta);
    }

    public Page<Venta> obtenerVentasPorTienda(String tiendaId, String buscar, Pageable pageable) {
        if (buscar != null && !buscar.trim().isEmpty()) {
            // Buscar por productos que contengan el texto
            return ventaRepository.findByTiendaIdAndProductosNombreContainingIgnoreCase(tiendaId, buscar, pageable);
        }
        return ventaRepository.findByTiendaId(tiendaId, pageable);
    }

}
