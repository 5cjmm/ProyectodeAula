package com.ShopMaster.Service;

import com.ShopMaster.Model.Deuda;
import com.ShopMaster.Repository.DeudaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
}
