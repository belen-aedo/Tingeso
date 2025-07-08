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
    public ResponseEntity<KartEntity> getKartByCodigo(@PathVariable String codigo) {
        return kartService.getKartByCodigo(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ CORREGIDO: Cambiar el mapeo para que sea más específico
    @PutMapping("/{codigo}/estado")
    public ResponseEntity<KartEntity> actualizarEstado(@PathVariable String codigo, @RequestBody KartEntity request) {
        System.out.println("PUT /kart/" + codigo + "/estado - Nuevo estado: " + request.getEstado());
        return kartService.cambiarEstadoKart(codigo, request.getEstado())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ CORREGIDO: Asegurar que el DELETE esté bien configurado
    @DeleteMapping("/{codigo}")
    public ResponseEntity<String> eliminarKart(@PathVariable String codigo) {
        System.out.println("DELETE /kart/" + codigo);
        try {
            kartService.deleteKart(codigo);
            return ResponseEntity.ok("Kart eliminado correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al eliminar kart: " + e.getMessage());
        }
    }
}