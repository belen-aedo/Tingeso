package com.example.demo.controllers;

import com.example.demo.entities.ReporteEntity;
import com.example.demo.entities.ReporteMensualEntity;
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

    // ===================== REPORTES INDIVIDUALES =====================

    @PostMapping("/individual")
    public ResponseEntity<?> crearReporte(@RequestBody ReporteEntity reporte) {
        try {
            ReporteEntity nuevoReporte = reporteService.guardarReporte(reporte);
            return ResponseEntity.ok(nuevoReporte);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear el reporte: " + e.getMessage());
        }
    }

    @GetMapping("/individual")
    public ResponseEntity<List<ReporteEntity>> obtenerTodosIndividuales() {
        return ResponseEntity.ok(reporteService.obtenerTodos());
    }

    @GetMapping("/individual/tipo/{tipo}")
    public ResponseEntity<List<ReporteEntity>> obtenerIndividualesPorTipo(@PathVariable String tipo) {
        return ResponseEntity.ok(reporteService.obtenerPorTipo(tipo));
    }

    @GetMapping("/individual/fecha")
    public ResponseEntity<Optional<ReporteEntity>> obtenerPorFecha(@RequestParam String fecha) {
        LocalDate fechaLocal = LocalDate.parse(fecha);
        return ResponseEntity.ok(reporteService.obtenerPorFecha(fechaLocal));
    }

    @GetMapping("/individual/mes-anio")
    public ResponseEntity<List<ReporteEntity>> obtenerPorMesYAnio(@RequestParam int mes, @RequestParam int anio) {
        return ResponseEntity.ok(reporteService.obtenerPorMesYAnio(mes, anio));
    }

    @GetMapping("/individual/{id}")
    public ResponseEntity<ReporteEntity> obtenerIndividualPorId(@PathVariable Long id) {
        Optional<ReporteEntity> reporte = reporteService.obtenerPorId(id);
        return reporte.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/individual/{id}")
    public ResponseEntity<?> eliminarReporteIndividual(@PathVariable Long id) {
        try {
            reporteService.eliminarReporte(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al eliminar el reporte: " + e.getMessage());
        }
    }

    // ===================== REPORTES MENSUALES =====================

    @GetMapping("/mensual")
    public ResponseEntity<List<ReporteMensualEntity>> obtenerReportesMensuales() {
        return ResponseEntity.ok(reporteService.obtenerReportesMensuales());
    }

    @GetMapping("/mensual/tipo/{tipo}")
    public ResponseEntity<List<ReporteMensualEntity>> obtenerMensualesPorTipo(@PathVariable String tipo) {
        return ResponseEntity.ok(reporteService.obtenerReportesMensualesPorTipo(tipo));
    }

    @GetMapping("/mensual/mes-anio")
    public ResponseEntity<List<ReporteMensualEntity>> obtenerMensualesPorMesYAnio(@RequestParam int mes, @RequestParam int anio) {
        return ResponseEntity.ok(reporteService.obtenerReportesMensualesPorMesYAnio(mes, anio));
    }

    @GetMapping("/mensual/anio/{anio}")
    public ResponseEntity<List<ReporteMensualEntity>> obtenerMensualesPorAnio(@PathVariable int anio) {
        return ResponseEntity.ok(reporteService.obtenerReportesMensualesPorAnio(anio));
    }
}