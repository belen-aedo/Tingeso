package com.example.demo.controllers;

import com.example.demo.entities.ComprobantePagoEntity;
import com.example.demo.service.ComprobantePagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@Transactional(readOnly = true)
@CrossOrigin("*")
@RequestMapping("/api/comprobantes")
public class ComprobantePagoController {

    private final ComprobantePagoService comprobantePagoService;

    @Autowired
    public ComprobantePagoController(ComprobantePagoService comprobantePagoService) {
        this.comprobantePagoService = comprobantePagoService;
    }

    @GetMapping
    public ResponseEntity<List<ComprobantePagoEntity>> obtenerTodos() {
        try {
            List<ComprobantePagoEntity> comprobantes = comprobantePagoService.getAllComprobantes();
            return ResponseEntity.ok(comprobantes);
        } catch (Exception e) {
            System.err.println("Error al obtener todos los comprobantes: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComprobantePagoEntity> obtenerPorId(@PathVariable Long id) {
        try {
            Optional<ComprobantePagoEntity> comprobante = comprobantePagoService.getComprobanteById(id);
            return comprobante.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.err.println("Error al obtener comprobante por ID " + id + ": " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/generar/{reservaId}")
    @Transactional // Solo para operaciones de escritura
    public ResponseEntity<ComprobantePagoEntity> generarComprobante(@PathVariable Long reservaId) {
        try {
            ComprobantePagoEntity comprobante = comprobantePagoService.generarComprobantePago(reservaId);
            return ResponseEntity.ok(comprobante);
        } catch (Exception e) {
            System.err.println("Error al generar comprobante para reserva " + reservaId + ": " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/enviar/{comprobanteId}")
    @Transactional // Solo para operaciones de escritura
    public ResponseEntity<String> enviarComprobante(@PathVariable Long comprobanteId) {
        try {
            comprobantePagoService.enviarComprobantePorEmail(comprobanteId);
            return ResponseEntity.ok("Comprobante enviado por correo con éxito.");
        } catch (Exception e) {
            System.err.println("Error al enviar comprobante " + comprobanteId + ": " + e.getMessage());
            return ResponseEntity.badRequest().body("Error al enviar comprobante: " + e.getMessage());
        }
    }

    // Nuevo endpoint para descargar PDF
    @GetMapping("/pdf/{comprobanteId}")
    public ResponseEntity<byte[]> descargarPDF(@PathVariable Long comprobanteId) {
        try {
            Optional<ComprobantePagoEntity> optComprobante = comprobantePagoService.getComprobanteById(comprobanteId);
            if (optComprobante.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            byte[] pdfBytes = comprobantePagoService.generarPDFPublico(optComprobante.get());

            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition", "attachment; filename=comprobante_" + comprobanteId + ".pdf")
                    .body(pdfBytes);

        } catch (Exception e) {
            System.err.println("Error al generar PDF para comprobante " + comprobanteId + ": " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}