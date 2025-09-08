package com.ShopMaster.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ShopMaster.Model.Deuda;
import com.ShopMaster.Repository.DeudaRepository;

@Service
public class DeudaService {

    
    @Autowired
    private DeudaRepository deudaRepository;

    public Deuda registrarDeuda(Deuda deuda) {
        return deudaRepository.save(deuda);
    }

    public List<Deuda> obtenerTodasLasDeudas() {
        return deudaRepository.findAll();
    }

    public Optional<Deuda> obtenerDeudaPorId(String id) {
        return deudaRepository.findById(id);
    }

    public Deuda registrarAbono(String deudaId, double monto) {
        Optional<Deuda> deudaOpt = deudaRepository.findById(deudaId);
        if (deudaOpt.isPresent()) {
            Deuda deuda = deudaOpt.get();
            deuda.registrarAbono(monto);
            return deudaRepository.save(deuda);
        }
        return null;
    }

    public void eliminar(String id) {
        deudaRepository.deleteById(id);
    }

    public Page<Deuda> obtenerDeudasPorTienda(String tiendaId, String buscar, String estado, Pageable pageable) {
        if (buscar != null && !buscar.trim().isEmpty() && estado != null && !estado.trim().isEmpty()) {
            return deudaRepository.findByTiendaIdAndNombreClienteContainingIgnoreCaseAndEstado(tiendaId, buscar, estado, pageable);
        } else if (buscar != null && !buscar.trim().isEmpty()) {
            return deudaRepository.findByTiendaIdAndNombreClienteContainingIgnoreCase(tiendaId, buscar, pageable);
        } else if (estado != null && !estado.trim().isEmpty()) {
            return deudaRepository.findByTiendaIdAndEstado(tiendaId, estado, pageable);
        }
        return deudaRepository.findByTiendaId(tiendaId, pageable);
    }

}