package com.example.demo.controllers;

import com.example.demo.entities.ComprobantePagoEntity;
import com.example.demo.service.ComprobantePagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController

@CrossOrigin("*")
@RequestMapping("/api/comprobantes")
public class ComprobantePagoController {

    private final ComprobantePagoService comprobantePagoService;

    @Autowired
    public ComprobantePagoController(ComprobantePagoService comprobantePagoService) {
        this.comprobantePagoService = comprobantePagoService;
    }

    @GetMapping
    public List<ComprobantePagoEntity> obtenerTodos() {
        return comprobantePagoService.getAllComprobantes();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComprobantePagoEntity> obtenerPorId(@PathVariable Long id) {
        Optional<ComprobantePagoEntity> comprobante = comprobantePagoService.getComprobanteById(id);
        return comprobante.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/generar/{reservaId}")
    public ResponseEntity<ComprobantePagoEntity> generarComprobante(@PathVariable Long reservaId) {
        try {
            ComprobantePagoEntity comprobante = comprobantePagoService.generarComprobantePago(reservaId);
            return ResponseEntity.ok(comprobante);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/enviar/{comprobanteId}")
    public ResponseEntity<String> enviarComprobante(@PathVariable Long comprobanteId) {
        try {
            comprobantePagoService.enviarComprobantePorEmail(comprobanteId);
            return ResponseEntity.ok("Comprobante enviado por correo con éxito.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al enviar comprobante: " + e.getMessage());
        }
    }
}