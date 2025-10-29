package com.ShopMaster.Controller;

import com.ShopMaster.Model.PQRS;
import com.ShopMaster.Service.PQRSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pqrs")
@CrossOrigin(origins = "*")
public class PQRSController {

    @Autowired
    private PQRSService pqrsService;

    @PostMapping
    public ResponseEntity<PQRS> enviarPQRS(@RequestBody PQRS pqrs) {
        PQRS guardado = pqrsService.enviarPQRS(pqrs);
        return ResponseEntity.ok(guardado);
    }
}
