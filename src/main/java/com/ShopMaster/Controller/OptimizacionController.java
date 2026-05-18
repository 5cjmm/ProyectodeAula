package com.ShopMaster.Controller;

import com.ShopMaster.Service.OptimizacionService;
import com.ShopMaster.dto.OptimizacionRequest;
import com.ShopMaster.dto.OptimizacionResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/optimizacion")
public class OptimizacionController {

    @Autowired
    private OptimizacionService optimizacionService;

    /**
     * POST /api/optimizacion/calcular
     *
     * Body esperado:
     * {
     *   "tiendaId":          "abc123",
     *   "inventarioTotal":   200,
     *   "presupuesto":       500,
     *   "creditoDisponible": 300,
     *   "capacidadVentas":   150
     * }
     */
    @PostMapping("/calcular")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENDERO')")
    public OptimizacionResponse calcular(@RequestBody OptimizacionRequest request) {
        return optimizacionService.optimizar(request);
    }
}
