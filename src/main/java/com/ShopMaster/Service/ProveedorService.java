package com.ShopMaster.Service;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ShopMaster.Model.Proveedor;
import com.ShopMaster.Repository.ProveedorRepository;

@Service
public class ProveedorService {
    
    @Autowired
    private ProveedorRepository proveedorRepository;

    public void guardarProveedor(Proveedor proveedor) {
        proveedorRepository.save(proveedor);
    } 

    public List<Proveedor> obtenerTodosLosProveedores() {
        return proveedorRepository.findAll();
    }

    

    public void actualizarProveedor(Proveedor proveedores) {
        if(proveedorRepository.existsById(proveedores.getId())) {
            proveedorRepository.save(proveedores);
        }
    }

    public void eliminarProveedor(ObjectId id) {
        proveedorRepository.deleteById(id);
    }


}
