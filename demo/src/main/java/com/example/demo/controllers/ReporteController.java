package com.example.demo.controllers;

import com.example.demo.entities.ReporteEntity;
import com.example.demo.service.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/reportes")
@CrossOrigin("*")
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    // Crear nuevo reporte

    @PostMapping("/")
    public ResponseEntity<?> crearReporte(@RequestBody ReporteEntity reporte) {
        ReporteEntity nuevoReporte = reporteService.guardarReporte(reporte);
        return ResponseEntity.ok(nuevoReporte);
    }




    // Obtener todos los reportes
    @GetMapping("/")
    public ResponseEntity<List<ReporteEntity>> obtenerTodos() {
        return ResponseEntity.ok(reporteService.obtenerTodos());
    }

    // Obtener por tipo (PorVueltas o PorPersonas)
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<ReporteEntity>> obtenerPorTipo(@PathVariable String tipo) {
        return ResponseEntity.ok(reporteService.obtenerPorTipo(tipo));
    }

    // Obtener por mes
    @GetMapping("/mes")
    public ResponseEntity<List<ReporteEntity>> obtenerPorMes(@RequestParam String fecha) {
        LocalDate mes = LocalDate.parse(fecha); // formato: YYYY-MM-DD
        return ResponseEntity.ok(reporteService.obtenerPorMes(mes));
    }

    // Obtener reporte por ID
    @GetMapping("/{id}")
    public ResponseEntity<ReporteEntity> obtenerPorId(@PathVariable Long id) {
        Optional<ReporteEntity> reporte = reporteService.obtenerPorId(id);
        return reporte.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarReporte(@PathVariable Long id) {
        reporteService.eliminarReporte(id);
        return ResponseEntity.noContent().build(); // 204 No Content, sin mensaje
    }
}
