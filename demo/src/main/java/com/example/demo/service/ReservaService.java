package com.example.demo.service;

import com.example.demo.entities.ClienteEntity;
import com.example.demo.entities.ReservaEntity;
import com.example.demo.repositories.ClienteRepository;
import com.example.demo.repositories.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    // NUEVA DEPENDENCIA: Inyectar el servicio de comprobantes
    @Autowired
    private ComprobantePagoService comprobantePagoService;

    // Obtener todas las reservas
    public List<ReservaEntity> obtenerTodasLasReservas() {
        return reservaRepository.findAll();
    }

    // Obtener reserva por ID
    public Optional<ReservaEntity> obtenerReservaPorId(Long idReserva) {
        return reservaRepository.findById(idReserva);
    }

    // Obtener reservas por día
    public List<ReservaEntity> obtenerReservasPorDia(LocalDate diaReserva) {
        return reservaRepository.findByDiaReserva(diaReserva);
    }

    // Obtener reservas por RUT de cliente
    public List<ReservaEntity> obtenerReservasPorCliente(String rutCliente) {
        return reservaRepository.findByClienteRut(rutCliente);
    }

    // MÉTODO MODIFICADO: Crear reserva con generación automática de comprobante
    public ReservaEntity crearReserva(ReservaEntity reserva) {
        try {
            // Verificar que el cliente existe
            Optional<ClienteEntity> clienteOpt = clienteRepository.findById(reserva.getCliente().getRut());
            if (clienteOpt.isEmpty()) {
                throw new RuntimeException("Cliente no encontrado");
            }

            // Calcular tiempo de reserva si no está establecido
            if (reserva.getTiempoReserva() <= 0) {
                reserva.setTiempoReserva(calcularTiempoReserva(reserva.getHoraInicio(), reserva.getHoraFin()));
            }

            // Guardar la reserva
            ReservaEntity reservaGuardada = reservaRepository.save(reserva);
            System.out.println("Reserva creada exitosamente con ID: " + reservaGuardada.getIdReserva());

            // NUEVO: Generar comprobante automáticamente
            try {
                comprobantePagoService.generarComprobantePago(reservaGuardada.getIdReserva());
                System.out.println("Comprobante generado automáticamente para la reserva: " + reservaGuardada.getIdReserva());
            } catch (Exception e) {
                System.err.println("Error al generar comprobante automático: " + e.getMessage());
                // No lanzamos la excepción para que la reserva se mantenga
                // Solo registramos el error
            }

            return reservaGuardada;

        } catch (Exception e) {
            System.err.println("Error al crear reserva: " + e.getMessage());
            throw new RuntimeException("Error al crear reserva: " + e.getMessage(), e);
        }
    }

    // NUEVO MÉTODO: Calcular tiempo de reserva en minutos
    private int calcularTiempoReserva(java.time.LocalTime horaInicio, java.time.LocalTime horaFin) {
        if (horaInicio == null || horaFin == null) {
            return 30; // Valor por defecto
        }

        java.time.Duration duration = java.time.Duration.between(horaInicio, horaFin);
        return (int) duration.toMinutes();
    }

    // Actualizar una reserva existente
    public ReservaEntity actualizarReserva(Long idReserva, ReservaEntity reservaActualizada) {
        Optional<ReservaEntity> reservaExistenteOpt = reservaRepository.findById(idReserva);
        if (reservaExistenteOpt.isEmpty()) {
            throw new RuntimeException("Reserva no encontrada");
        }

        ReservaEntity reservaExistente = reservaExistenteOpt.get();

        // Verificar y asignar el cliente si se ha modificado
        if (reservaActualizada.getCliente() != null &&
                !reservaExistente.getCliente().getRut().equals(reservaActualizada.getCliente().getRut())) {

            Optional<ClienteEntity> clienteOpt = clienteRepository.findById(reservaActualizada.getCliente().getRut());
            if (clienteOpt.isEmpty()) {
                throw new RuntimeException("Cliente no encontrado");
            }
            reservaExistente.setCliente(clienteOpt.get());
        }

        // Actualizar los demás campos
        if (reservaActualizada.getDiaReserva() != null) {
            reservaExistente.setDiaReserva(reservaActualizada.getDiaReserva());
        }
        if (reservaActualizada.getHoraInicio() != null) {
            reservaExistente.setHoraInicio(reservaActualizada.getHoraInicio());
        }
        if (reservaActualizada.getHoraFin() != null) {
            reservaExistente.setHoraFin(reservaActualizada.getHoraFin());
        }
        if (reservaActualizada.getAcompanantes() != null) {
            reservaExistente.setAcompanantes(reservaActualizada.getAcompanantes());
        }

        // Recalcular tiempo de reserva
        reservaExistente.setTiempoReserva(calcularTiempoReserva(
                reservaExistente.getHoraInicio(),
                reservaExistente.getHoraFin()
        ));

        return reservaRepository.save(reservaExistente);
    }

    // DTO para calendario
    public static class ReservaDTO {
        public Long id;
        public String cliente;
        public String start;
        public String end;
        public String title;

        public ReservaDTO(Long id, String cliente, String start, String end, String title) {
            this.id = id;
            this.cliente = cliente;
            this.start = start;
            this.end = end;
            this.title = title;
        }
    }

    public List<ReservaDTO> obtenerReservasDTO() {
        return reservaRepository.findAll().stream().map(reserva -> {
            LocalDateTime start = reserva.getDiaReserva().atTime(reserva.getHoraInicio());
            LocalDateTime end = reserva.getDiaReserva().atTime(reserva.getHoraFin());

            String cliente = reserva.getCliente().getNombre();
            String title = "Cliente: " + cliente;

            return new ReservaDTO(reserva.getIdReserva(), cliente, start.toString(), end.toString(), title);
        }).collect(Collectors.toList());
    }

    // Eliminar una reserva
    public void eliminarReserva(Long idReserva) {
        reservaRepository.deleteById(idReserva);
    }

    // NUEVO MÉTODO: Generar comprobantes para reservas existentes sin comprobante
    public void generarComprobantesFaltantes() {
        try {
            List<ReservaEntity> todasLasReservas = reservaRepository.findAll();
            int comprobantesGenerados = 0;

            for (ReservaEntity reserva : todasLasReservas) {
                try {
                    // Verificar si ya existe un comprobante para esta reserva
                    Optional<com.example.demo.entities.ComprobantePagoEntity> comprobanteExistente =
                            comprobantePagoService.getComprobanteByReservaId(reserva.getIdReserva());

                    if (comprobanteExistente.isEmpty()) {
                        // Generar comprobante faltante
                        comprobantePagoService.generarComprobantePago(reserva.getIdReserva());
                        comprobantesGenerados++;
                        System.out.println("Comprobante generado para reserva: " + reserva.getIdReserva());
                    }
                } catch (Exception e) {
                    System.err.println("Error generando comprobante para reserva " +
                            reserva.getIdReserva() + ": " + e.getMessage());
                }
            }

            System.out.println("Proceso completado. Comprobantes generados: " + comprobantesGenerados);

        } catch (Exception e) {
            System.err.println("Error en el proceso de generación de comprobantes faltantes: " + e.getMessage());
            throw new RuntimeException("Error generando comprobantes faltantes", e);
        }
    }
}