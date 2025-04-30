package com.example.demo.controllers;

import com.example.demo.entities.ClienteEntity;
import com.example.demo.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/clientes")
@CrossOrigin("*") // Para permitir peticiones desde cualquier origen (útil para frontend)
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    // Obtener todos los clientes
    @GetMapping("/")
    public ResponseEntity<List<ClienteEntity>> getAllClientes() {
        return ResponseEntity.ok(clienteService.getAllClientes());
    }

    // Obtener cliente por RUT
    @GetMapping("/{rut}")
    public ResponseEntity<ClienteEntity> getClienteByRut(@PathVariable String rut) {
        Optional<ClienteEntity> cliente = clienteService.getClienteById(rut);
        return cliente.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // Obtener cliente por email
    @GetMapping("/email/{email}")
    public ResponseEntity<ClienteEntity> getClienteByEmail(@PathVariable String email) {
        Optional<ClienteEntity> cliente = clienteService.getClienteByEmail(email);
        return cliente.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // Obtener clientes por rango de visitas
    @GetMapping("/visitas")
    public ResponseEntity<List<ClienteEntity>> getClientesByVisitas(
            @RequestParam int min,
            @RequestParam int max
    ) {
        return ResponseEntity.ok(clienteService.getClientesByRangoVisitas(min, max));
    }

    // Crear o actualizar cliente
    @PostMapping("/")
    public ResponseEntity<ClienteEntity> saveCliente(@RequestBody ClienteEntity cliente) {
        ClienteEntity saved = clienteService.saveCliente(cliente);
        return ResponseEntity.ok(saved);
    }

    // Incrementar visitas del cliente
    @PutMapping("/incrementar-visita/{rut}")
    public ResponseEntity<Void> incrementarVisita(@PathVariable String rut) {
        clienteService.incrementarVisitaCliente(rut);
        return ResponseEntity.ok().build();
    }

    // Resetear visitas mensuales de todos los clientes
    @PutMapping("/resetear-visitas")
    public ResponseEntity<Void> resetearVisitas() {
        clienteService.resetearVisitasMensuales();
        return ResponseEntity.ok().build();
    }

    // Eliminar cliente
    @DeleteMapping("/{rut}")
    public ResponseEntity<Void> deleteCliente(@PathVariable String rut) {
        clienteService.deleteCliente(rut);
        return ResponseEntity.ok().build();
    }
}
