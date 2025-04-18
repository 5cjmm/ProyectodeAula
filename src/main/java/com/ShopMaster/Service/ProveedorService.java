package com.ShopMaster.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ShopMaster.Model.Proveedores;
import com.ShopMaster.Repository.ProveedorRepository;

@Service
public class ProveedorService {
    
    @Autowired
    private ProveedorRepository proveedorRepository;

    public void guardarProveedor(Proveedores proveedor) {
        proveedorRepository.save(proveedor);
    } 

    public List<Proveedores> obtenerTodosLosProveedores() {
        return proveedorRepository.findAll();
    }

    

    public void actualizarProveedor(Proveedores proveedores) {
        if(proveedorRepository.existsById(proveedores.getId())) {
            proveedorRepository.save(proveedores);
        }
    }

    public void eliminarProveedor(String id) {
        proveedorRepository.deleteById(id);
    }


}
