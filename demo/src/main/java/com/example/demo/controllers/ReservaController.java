package com.example.demo.controllers;

import com.example.demo.entities.ReservaEntity;
import com.example.demo.service.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@CrossOrigin("*")

@RequestMapping("/reservas")
public class ReservaController {

    @Autowired
    private ReservaService reservaService;

    // Obtener todas las reservas
    @GetMapping
    public ResponseEntity<List<ReservaEntity>> obtenerTodasLasReservas() {
        return ResponseEntity.ok(reservaService.obtenerTodasLasReservas());
    }

    // Obtener reserva por ID
    @GetMapping("/{id}")
    public ResponseEntity<ReservaEntity> obtenerReservaPorId(@PathVariable Long id) {
        return reservaService.obtenerReservaPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Obtener reservas por día
    @GetMapping("/dia/{dia}")
    public ResponseEntity<List<ReservaEntity>> obtenerReservasPorDia(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dia) {
        return ResponseEntity.ok(reservaService.obtenerReservasPorDia(dia));
    }

    // Obtener reservas por cliente
    @GetMapping("/cliente/{rutCliente}")
    public ResponseEntity<List<ReservaEntity>> obtenerReservasPorCliente(@PathVariable String rutCliente) {
        return ResponseEntity.ok(reservaService.obtenerReservasPorCliente(rutCliente));
    }

    // Crear una nueva reserva
    @PostMapping
    public ResponseEntity<ReservaEntity> crearReserva(@RequestBody ReservaEntity reserva) {
        try {
            ReservaEntity nuevaReserva = reservaService.crearReserva(reserva);
            return new ResponseEntity<>(nuevaReserva, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Actualizar una reserva
    @PutMapping("/{id}")
    public ResponseEntity<ReservaEntity> actualizarReserva(
            @PathVariable Long id,
            @RequestBody ReservaEntity reserva) {
        try {
            ReservaEntity reservaActualizada = reservaService.actualizarReserva(id, reserva);
            return ResponseEntity.ok(reservaActualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Eliminar una reserva
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarReserva(@PathVariable Long id) {
        try {
            reservaService.eliminarReserva(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Obtener reservas como DTO para calendario
    @GetMapping("/dto")
    public ResponseEntity<List<ReservaService.ReservaDTO>> obtenerReservasDTO() {
        return ResponseEntity.ok(reservaService.obtenerReservasDTO());
    }

}