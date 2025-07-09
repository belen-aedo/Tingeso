package com.example.demo.controllers;

import com.example.demo.entities.ClienteEntity;
import com.example.demo.service.ClienteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/clientes")
@CrossOrigin("*") // Para permitir peticiones desde cualquier origen (útil para frontend)
public class ClienteController {

    private static final Logger logger = LoggerFactory.getLogger(ClienteController.class);
    private static final String ERROR_INTERNO_SERVIDOR = "Error interno del servidor";
    private static final String ERROR_INTERNO_ELIMINAR = "Error interno al eliminar cliente";

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    // Obtener todos los clientes
    @GetMapping("/")
    public ResponseEntity<List<ClienteEntity>> getAllClientes() {
        try {
            List<ClienteEntity> clientes = clienteService.getAllClientes();
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            logger.error("Error al obtener clientes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Obtener cliente por RUT
    @GetMapping("/{rut}")
    public ResponseEntity<Object> getClienteByRut(@PathVariable String rut) {
        try {
            Optional<ClienteEntity> cliente = clienteService.getClienteByRutValidado(rut);
            if (cliente.isPresent()) {
                return ResponseEntity.ok(cliente.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Cliente no encontrado con RUT: " + rut);
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("RUT inválido: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error al obtener cliente por RUT", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ERROR_INTERNO_SERVIDOR);
        }
    }

    // Obtener cliente por email
    @GetMapping("/email/{email}")
    public ResponseEntity<Object> getClienteByEmail(@PathVariable String email) {
        try {
            Optional<ClienteEntity> cliente = clienteService.getClienteByEmail(email);
            if (cliente.isPresent()) {
                return ResponseEntity.ok(cliente.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Cliente no encontrado con email: " + email);
            }
        } catch (Exception e) {
            logger.error("Error al obtener cliente por email", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ERROR_INTERNO_SERVIDOR);
        }
    }

    // Obtener clientes por rango de visitas
    @GetMapping("/visitas")
    public ResponseEntity<Object> getClientesByVisitas(
            @RequestParam int min,
            @RequestParam int max
    ) {
        try {
            if (min < 0 || max < 0 || min > max) {
                return ResponseEntity.badRequest()
                        .body("Parámetros de rango inválidos");
            }
            List<ClienteEntity> clientes = clienteService.getClientesByRangoVisitas(min, max);
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            logger.error("Error al obtener clientes por visitas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ERROR_INTERNO_SERVIDOR);
        }
    }

    // Crear o actualizar cliente
    @PostMapping("/")
    public ResponseEntity<Object> saveCliente(@RequestBody ClienteEntity cliente) {
        try {
            // Validaciones básicas
            if (cliente.getRut() == null || cliente.getRut().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("RUT es requerido");
            }
            if (cliente.getNombre() == null || cliente.getNombre().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Nombre es requerido");
            }
            if (cliente.getEmail() == null || cliente.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Email es requerido");
            }

            ClienteEntity saved = clienteService.saveCliente(cliente);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al crear cliente", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al crear cliente");
        }
    }

    // Incrementar visitas del cliente
    @PutMapping("/incrementar-visita/{rut}")
    public ResponseEntity<String> incrementarVisita(@PathVariable String rut) {
        try {
            clienteService.incrementarVisitaCliente(rut);
            return ResponseEntity.ok("Visita incrementada correctamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al incrementar visita", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al incrementar visita");
        }
    }

    // Resetear visitas mensuales de todos los clientes
    @PutMapping("/resetear-visitas")
    public ResponseEntity<String> resetearVisitas() {
        try {
            clienteService.resetearVisitasMensuales();
            return ResponseEntity.ok("Visitas mensuales reseteadas correctamente");
        } catch (Exception e) {
            logger.error("Error al resetear visitas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al resetear visitas");
        }
    }

    @DeleteMapping("/{rut}")
    public ResponseEntity<String> deleteCliente(@PathVariable String rut) {
        try {
            logger.info("Intentando eliminar cliente con RUT: {}", rut);

            // Verificar que el cliente existe antes de eliminar
            Optional<ClienteEntity> cliente = clienteService.getClienteByRutValidado(rut);
            if (!cliente.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Cliente no encontrado con RUT: " + rut);
            }

            clienteService.deleteCliente(rut);
            return ResponseEntity.ok("Cliente eliminado correctamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al eliminar cliente", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ERROR_INTERNO_ELIMINAR);
        }
    }

    // Endpoint adicional para validar RUT
    @GetMapping("/validar-rut/{rut}")
    public ResponseEntity<Object> validarRut(@PathVariable String rut) {
        try {
            boolean esValido = clienteService.esRutValido(rut);
            if (esValido) {
                String rutNormalizado = clienteService.normalizarRut(rut);
                return ResponseEntity.ok(new RutValidationResponse(true, rutNormalizado, "RUT válido"));
            } else {
                return ResponseEntity.ok(new RutValidationResponse(false, null, "RUT inválido"));
            }
        } catch (Exception e) {
            logger.error("Error al validar RUT", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al validar RUT");
        }
    }

    // Clase auxiliar para respuesta de validación
    public static class RutValidationResponse {
        private boolean valido;
        private String rutNormalizado;
        private String mensaje;

        public RutValidationResponse(boolean valido, String rutNormalizado, String mensaje) {
            this.valido = valido;
            this.rutNormalizado = rutNormalizado;
            this.mensaje = mensaje;
        }

        // Getters y setters
        public boolean isValido() { return valido; }
        public void setValido(boolean valido) { this.valido = valido; }
        public String getRutNormalizado() { return rutNormalizado; }
        public void setRutNormalizado(String rutNormalizado) { this.rutNormalizado = rutNormalizado; }
        public String getMensaje() { return mensaje; }
        public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    }
}