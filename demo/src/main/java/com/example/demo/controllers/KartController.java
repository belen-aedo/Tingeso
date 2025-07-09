package com.example.demo.controllers;

import com.example.demo.entities.KartEntity;
import com.example.demo.service.KartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/kart")
@CrossOrigin("*")
public class KartController {
    
    private static final Logger logger = LoggerFactory.getLogger(KartController.class);
    private final KartService kartService;

    public KartController(KartService kartService) {
        this.kartService = kartService;
    }

    @PostMapping("/")
    public ResponseEntity<KartEntity> saveKart(@RequestBody KartEntity kart) {
        try {
            KartEntity savedKart = kartService.saveKart(kart);
            return ResponseEntity.ok(savedKart);
        } catch (Exception e) {
            logger.error("Error al guardar el kart: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/")
    public ResponseEntity<List<KartEntity>> getAll(){
        try {
            List<KartEntity> karts = kartService.getAllKarts();
            return ResponseEntity.ok(karts);
        } catch (Exception e) {
            logger.error("Error al obtener todos los karts: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<KartEntity> getKartByCodigo(@PathVariable String codigo) {
        try {
            return kartService.getKartByCodigo(codigo)
                    .map(kart -> ResponseEntity.status(HttpStatus.OK).body(kart))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (Exception e) {
            logger.error("Error al obtener kart por código {}: {}", codigo, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{codigo}/estado")
    public ResponseEntity<KartEntity> actualizarEstado(@PathVariable String codigo, @RequestBody KartEntity request) {
        try {
            logger.info("Actualizando estado del kart {} a: {}", codigo, request.getEstado());
            return kartService.cambiarEstadoKart(codigo, request.getEstado())
                    .map(kart -> ResponseEntity.status(HttpStatus.OK).body(kart))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (Exception e) {
            logger.error("Error al actualizar estado del kart {}: {}", codigo, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<String> eliminarKart(@PathVariable String codigo) {
        try {
            logger.info("Eliminando kart con código: {}", codigo);
            kartService.deleteKart(codigo);
            return ResponseEntity.ok("Kart eliminado correctamente");
        } catch (RuntimeException e) {
            logger.warn("Kart no encontrado para eliminar: {}", codigo);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error al eliminar kart {}: {}", codigo, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar kart: " + e.getMessage());
        }
    }
}