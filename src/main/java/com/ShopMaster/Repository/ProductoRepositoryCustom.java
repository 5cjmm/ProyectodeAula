package com.ShopMaster.Repository;

import java.util.List;

import com.ShopMaster.Model.ProductoConProveedores;

public interface ProductoRepositoryCustom {
    List<ProductoConProveedores> obtenerProductosConProveedores();
}
