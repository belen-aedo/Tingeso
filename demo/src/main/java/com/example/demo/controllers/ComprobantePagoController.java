package com.example.demo.controllers;

import com.example.demo.entities.ComprobantePagoEntity;
import com.example.demo.service.ComprobantePagoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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
    private static final String LOG_MESSAGE_FORMAT = "{} {}: {}";
    private static final Logger logger = LoggerFactory.getLogger(ComprobantePagoController.class);
    private static final String ERROR_OBTENER_COMPROBANTES = "Error al obtener todos los comprobantes";
    private static final String ERROR_OBTENER_COMPROBANTE_ID = "Error al obtener comprobante por ID";
    private static final String ERROR_GENERAR_COMPROBANTE = "Error al generar comprobante para reserva";
    private static final String ERROR_ENVIAR_COMPROBANTE = "Error al enviar comprobante";
    private static final String ERROR_GENERAR_PDF = "Error al generar PDF para comprobante";

    private final ComprobantePagoService comprobantePagoService;

    public ComprobantePagoController(ComprobantePagoService comprobantePagoService) {
        this.comprobantePagoService = comprobantePagoService;
    }

    @GetMapping
    public ResponseEntity<List<ComprobantePagoEntity>> obtenerTodos() {
        try {
            List<ComprobantePagoEntity> comprobantes = comprobantePagoService.getAllComprobantes();
            return ResponseEntity.ok(comprobantes);
        } catch (Exception e) {
            logger.error("{}: {}", ERROR_OBTENER_COMPROBANTES, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComprobantePagoEntity> obtenerPorId(@PathVariable Long id) {

        try {
            Optional<ComprobantePagoEntity> comprobante = comprobantePagoService.getComprobanteById(id);
            return comprobante.map(c -> ResponseEntity.status(HttpStatus.OK).body(c))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (Exception e) {
            logger.error(LOG_MESSAGE_FORMAT, ERROR_OBTENER_COMPROBANTE_ID, id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/generar/{reservaId}")
    @Transactional // Solo para operaciones de escritura
    public ResponseEntity<ComprobantePagoEntity> generarComprobantePago(@PathVariable Long reservaId) {
        try {
            ComprobantePagoEntity comprobante = comprobantePagoService.generarComprobantePago(reservaId);
            return ResponseEntity.ok(comprobante);
        } catch (Exception e) {
            logger.error(LOG_MESSAGE_FORMAT, ERROR_GENERAR_COMPROBANTE, reservaId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/enviar/{comprobanteId}")
    @Transactional // Solo para operaciones de escritura
    public ResponseEntity<String> enviarComprobante(@PathVariable Long comprobanteId) {
        try {
            comprobantePagoService.enviarComprobantePorEmail(comprobanteId);
            return ResponseEntity.ok("Comprobante enviado por correo con éxito.");
        } catch (Exception e) {
            logger.error(LOG_MESSAGE_FORMAT, ERROR_ENVIAR_COMPROBANTE, comprobanteId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al enviar comprobante: " + e.getMessage());
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
            logger.error(LOG_MESSAGE_FORMAT, ERROR_GENERAR_PDF, comprobanteId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}