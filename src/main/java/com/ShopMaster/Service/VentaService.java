package com.ShopMaster.Service;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
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

    public void eliminarVenta(ObjectId id) {
        ventaRepository.deleteById(id);
    }

}
