package com.example.demo.controllers;

import com.example.demo.entities.KartEntity;
import com.example.demo.service.KartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/kart")
@CrossOrigin("*")
public class KartController {
    @Autowired
    KartService kartService;

    @PostMapping("/")
    public ResponseEntity<KartEntity> saveKart(@RequestBody KartEntity kart) {
        KartEntity savedKart = kartService.saveKart(kart);
        return ResponseEntity.ok(savedKart);
    }

    @GetMapping("/")
    public ResponseEntity<List<KartEntity>> getAll(){
        List<KartEntity> karts = kartService.getAllKarts();
        return ResponseEntity.ok(karts);
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<KartEntity> getKartById(@PathVariable String id, @PathVariable String codigo) { // Cambiado a String
        return kartService.getKartByCodigo(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
