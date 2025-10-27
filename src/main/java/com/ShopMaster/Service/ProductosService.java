package com.ShopMaster.Service;

import java.util.Collections;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ShopMaster.Model.ProductoConProveedores;
import com.ShopMaster.Model.Productos;
import com.ShopMaster.Model.Proveedor;
import com.ShopMaster.Repository.ProductosRepository;
import com.ShopMaster.Repository.ProveedorRepository;

@Service
public class ProductosService {

    @Autowired
    private ProductosRepository productosRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    // Crear producto asociado a una tienda
    public Productos guardarProducto(Productos producto, String tiendaId) {
        if (productosRepository.existsByCodigoAndTiendaId(producto.getCodigo(), producto.getTiendaId())) {
            throw new RuntimeException("Ya existe un producto con ese código en esta tienda");
        }

        return productosRepository.save(producto);
    }

    // Listar productos de una tienda
   /*  public Page<Productos> obtenerProductosPorTienda(String tiendaId, int page, int size) {
        return productosRepository.findByTiendaId(tiendaId, PageRequest.of(page, size));
    }*/

    // Actualizar producto asegurando que pertenece a la tienda
    public Productos actualizarProducto(String id, Productos producto) {
    Productos existente = productosRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

    // 🧩 Validar código duplicado solo si pertenece a otro producto en la misma tienda
    boolean codigoDuplicado = productosRepository
        .existsByCodigoAndTiendaIdAndIdNot(producto.getCodigo(), existente.getTiendaId(), id);

    if (codigoDuplicado) {
        throw new RuntimeException("El código ya está registrado 🚫");
    }

    // 🧠 Mantener la tienda original
    producto.setId(id);
    producto.setTiendaId(existente.getTiendaId());

    // 🧩 Si no se envían proveedores nuevos, mantener los actuales
    if (producto.getProveedorIds() == null || producto.getProveedorIds().isEmpty()) {
        producto.setProveedorIds(existente.getProveedorIds());
    }

    return productosRepository.save(producto);
}


    // Eliminar producto asegurando que pertenece a la tienda
    public void eliminarProducto(String id, String tiendaId) {
        Productos existente = productosRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (!existente.getTiendaId().equals(tiendaId)) {
            throw new RuntimeException("No tienes permiso para eliminar este producto");
        }

        productosRepository.deleteById(id);
    }


    public Page<ProductoConProveedores> listarPorTienda(String tiendaId, Pageable pageable) {
        Page<Productos> productosPage = productosRepository.findByTiendaId(tiendaId, pageable);

        return productosPage.map(producto -> {
            ProductoConProveedores dto = new ProductoConProveedores();
            dto.setId(producto.getId());
            dto.setCodigo(producto.getCodigo());
            dto.setNombre(producto.getNombre());
            dto.setCantidad(producto.getCantidad());
            dto.setPrecio(producto.getPrecio());
            dto.setTiendaId(producto.getTiendaId());
            dto.setProveedorIds(producto.getProveedorIds());

            if (producto.getProveedorIds() != null && !producto.getProveedorIds().isEmpty()) {
                List<Proveedor> proveedores = proveedorRepository.findAllById(
                        producto.getProveedorIds()
                                .stream()
                                .map(ObjectId::toHexString)
                                .toList()
                );
                dto.setProveedores(proveedores);
            } else {
                dto.setProveedores(Collections.emptyList());
            }

            return dto;
        });
    }
}
