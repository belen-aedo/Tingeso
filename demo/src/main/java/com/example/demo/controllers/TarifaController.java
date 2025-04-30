package com.example.demo.controllers;
import com.example.demo.entities.TarifaEntity;
import com.example.demo.service.TarifaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tarifas")
@CrossOrigin(origins = "*") // Esto permite llamadas desde Postman o frontend externo
public class TarifaController {

    private final TarifaService tarifaService;

    @Autowired
    public TarifaController(TarifaService tarifaService) {
        this.tarifaService = tarifaService;
    }

    // Crear nueva tarifa
    @PostMapping
    public TarifaEntity crearTarifa(@RequestBody TarifaEntity tarifa) {
        return tarifaService.saveTarifa(tarifa);
    }

    // Obtener todas las tarifas
    @GetMapping
    public List<TarifaEntity> obtenerTodasLasTarifas() {
        return tarifaService.getAllTarifas();
    }

    // Obtener una tarifa por ID
    @GetMapping("/{id}")
    public TarifaEntity obtenerTarifaPorId(@PathVariable Long id) {
        return tarifaService.getTarifaById(id).orElse(null);
    }

    // Obtener tarifa por número de vueltas
    @GetMapping("/buscarPorVueltas/{numeroVueltas}")
    public TarifaEntity obtenerPorNumeroVueltas(@PathVariable int numeroVueltas) {
        return tarifaService.getTarifaByNumeroVueltas(numeroVueltas).orElse(null);
    }

    // Obtener por número de vueltas y tiempo máximo
    @GetMapping("/buscarPorVueltasYTiempo")
    public TarifaEntity obtenerPorVueltasYTiempo(@RequestParam int numeroVueltas, @RequestParam int tiempoMaximo) {
        return tarifaService.getTarifaByNumeroVueltasAndTiempoMaximo(numeroVueltas, tiempoMaximo).orElse(null);
    }

    // Obtener tarifas dentro de un rango de precio
    @GetMapping("/rango-precio")
    public List<TarifaEntity> obtenerPorRangoPrecio(@RequestParam int precioMin, @RequestParam int precioMax) {
        return tarifaService.getTarifasByRangoPrecio(precioMin, precioMax);
    }

    // Obtener todas ordenadas por precio ascendente
    @GetMapping("/ordenadas-precio")
    public List<TarifaEntity> obtenerOrdenadasPorPrecio() {
        return tarifaService.getTarifasOrderedByPrecio();
    }

    // Actualizar tarifa existente
    @PutMapping("/{id}")
    public TarifaEntity actualizarTarifa(@PathVariable Long id, @RequestBody TarifaEntity tarifaActualizada) {
        return tarifaService.actualizarTarifa(id, tarifaActualizada);
    }

    // Eliminar tarifa
    @DeleteMapping("/{id}")
    public void eliminarTarifa(@PathVariable Long id) {
        tarifaService.deleteTarifa(id);
    }
}
