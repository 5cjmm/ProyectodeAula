package com.ShopMaster.Repository;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.stereotype.Repository;

import com.ShopMaster.Model.ProductoConProveedores;

@Repository
public class ProductoRepositoryImpl implements ProductoRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<ProductoConProveedores> obtenerProductosConProveedores() {

        LookupOperation lookup = LookupOperation.newLookup()
            .from("proveedores")             // nombre de la colección
            .localField("proveedorIds")     // campo en productos
            .foreignField("_id")            // campo en proveedores
            .as("proveedores");             // resultado como lista

        Aggregation aggregation = Aggregation.newAggregation(lookup);

        return mongoTemplate.aggregate(aggregation, "productos", ProductoConProveedores.class)
                            .getMappedResults();
    }
}
