package com.example.demo.service;

import com.example.demo.entities.ClienteEntity;
import com.example.demo.entities.ReservaEntity;
import com.example.demo.repositories.ClienteRepository;
import com.example.demo.repositories.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReservaService {

    private static final Logger logger = LoggerFactory.getLogger(ReservaService.class);

    private final ReservaRepository reservaRepository;
    private final ClienteRepository clienteRepository;
    private final ComprobantePagoService comprobantePagoService;

    // Constructor injection en lugar de @Autowired
    @Autowired
    public ReservaService(ReservaRepository reservaRepository,
                          ClienteRepository clienteRepository,
                          ComprobantePagoService comprobantePagoService) {
        this.reservaRepository = reservaRepository;
        this.clienteRepository = clienteRepository;
        this.comprobantePagoService = comprobantePagoService;
    }

    // Excepciones específicas
    public static class ReservaException extends RuntimeException {
        public ReservaException(String message) {
            super(message);
        }

        public ReservaException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class ClienteNoEncontradoException extends ReservaException {
        public ClienteNoEncontradoException(String message) {
            super(message);
        }
    }

    public static class HorarioNoDisponibleException extends ReservaException {
        public HorarioNoDisponibleException(String message) {
            super(message);
        }
    }

    public static class HorarioInvalidoException extends ReservaException {
        public HorarioInvalidoException(String message) {
            super(message);
        }
    }

    public static class ReservaNoEncontradaException extends ReservaException {
        public ReservaNoEncontradaException(String message) {
            super(message);
        }
    }

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

    // NUEVO MÉTODO: Validar disponibilidad de horario
    public boolean validarDisponibilidadHorario(LocalDate diaReserva, LocalTime horaInicio, LocalTime horaFin, Long idReservaExcluir) {
        // Obtener todas las reservas del día
        List<ReservaEntity> reservasDelDia = reservaRepository.findByDiaReserva(diaReserva);

        // Filtrar la reserva que se está actualizando (si aplica)
        if (idReservaExcluir != null) {
            reservasDelDia = reservasDelDia.stream()
                    .filter(reserva -> !reserva.getIdReserva().equals(idReservaExcluir))
                    .toList();
        }

        // Verificar si hay conflictos de horario
        return reservasDelDia.stream().noneMatch(reservaExistente -> {
            LocalTime inicioExistente = reservaExistente.getHoraInicio();
            LocalTime finExistente = reservaExistente.getHoraFin();

            // Verificar si hay solapamiento de horarios
            // Hay conflicto si:
            // 1. La nueva reserva empieza antes de que termine la existente Y
            // 2. La nueva reserva termina después de que empiece la existente
            return horaInicio.isBefore(finExistente) && horaFin.isAfter(inicioExistente);
        });
    }

    // Sobrecarga del método para nuevas reservas (sin ID a excluir)
    public boolean validarDisponibilidadHorario(LocalDate diaReserva, LocalTime horaInicio, LocalTime horaFin) {
        return validarDisponibilidadHorario(diaReserva, horaInicio, horaFin, null);
    }

    // MÉTODO MODIFICADO: Crear reserva con validación de horario y generación automática de comprobante
    public ReservaEntity crearReserva(ReservaEntity reserva) {
        try {
            validarClienteExiste(reserva);
            validarHorarios(reserva);
            validarDisponibilidadHorarioParaCreacion(reserva);

            // Calcular tiempo de reserva si no está establecido
            if (reserva.getTiempoReserva() <= 0) {
                reserva.setTiempoReserva(calcularTiempoReserva(reserva.getHoraInicio(), reserva.getHoraFin()));
            }

            // Guardar la reserva
            ReservaEntity reservaGuardada = reservaRepository.save(reserva);
            logger.info("Reserva creada exitosamente con ID: {}", reservaGuardada.getIdReserva());

            return reservaGuardada;

        } catch (ReservaException e) {
            throw e; // Re-lanzar la excepción específica
        } catch (Exception e) {
            throw new ReservaException("Error al crear reserva: " + e.getMessage(), e);
        }
    }

    private void validarClienteExiste(ReservaEntity reserva) {
        Optional<ClienteEntity> clienteOpt = clienteRepository.findById(reserva.getCliente().getRut());
        if (clienteOpt.isEmpty()) {
            throw new ClienteNoEncontradoException("Cliente no encontrado");
        }
    }

    private void validarHorarios(ReservaEntity reserva) {
        if (reserva.getHoraInicio() == null || reserva.getHoraFin() == null) {
            throw new HorarioInvalidoException("Debe especificar hora de inicio y fin");
        }

        if (reserva.getHoraInicio().isAfter(reserva.getHoraFin()) ||
                reserva.getHoraInicio().equals(reserva.getHoraFin())) {
            throw new HorarioInvalidoException("La hora de inicio debe ser anterior a la hora de fin");
        }
    }

    private void validarDisponibilidadHorarioParaCreacion(ReservaEntity reserva) {
        if (!validarDisponibilidadHorario(reserva.getDiaReserva(), reserva.getHoraInicio(), reserva.getHoraFin())) {
            throw new HorarioNoDisponibleException("El horario seleccionado no está disponible. Ya existe una reserva que se superpone con este horario.");
        }
    }

    // NUEVO MÉTODO: Calcular tiempo de reserva en minutos
    private int calcularTiempoReserva(LocalTime horaInicio, LocalTime horaFin) {
        if (horaInicio == null || horaFin == null) {
            return 30; // Valor por defecto
        }

        java.time.Duration duration = java.time.Duration.between(horaInicio, horaFin);
        return (int) duration.toMinutes();
    }

    // MÉTODO MODIFICADO: Actualizar una reserva existente con validación de horario
    public ReservaEntity actualizarReserva(Long idReserva, ReservaEntity reservaActualizada) {
        Optional<ReservaEntity> reservaExistenteOpt = reservaRepository.findById(idReserva);
        if (reservaExistenteOpt.isEmpty()) {
            throw new ReservaNoEncontradaException("Reserva no encontrada");
        }

        ReservaEntity reservaExistente = reservaExistenteOpt.get();

        // Verificar y asignar el cliente si se ha modificado
        actualizarClienteSiEsNecesario(reservaExistente, reservaActualizada);

        // Preparar los valores para la validación de horario
        LocalDate diaReserva = reservaActualizada.getDiaReserva() != null ?
                reservaActualizada.getDiaReserva() : reservaExistente.getDiaReserva();
        LocalTime horaInicio = reservaActualizada.getHoraInicio() != null ?
                reservaActualizada.getHoraInicio() : reservaExistente.getHoraInicio();
        LocalTime horaFin = reservaActualizada.getHoraFin() != null ?
                reservaActualizada.getHoraFin() : reservaExistente.getHoraFin();

        // Validar horarios si se han modificado
        validarHorariosActualizacion(reservaActualizada, diaReserva, horaInicio, horaFin, idReserva);

        // Actualizar los demás campos
        actualizarCamposReserva(reservaExistente, reservaActualizada);

        // Recalcular tiempo de reserva
        reservaExistente.setTiempoReserva(calcularTiempoReserva(
                reservaExistente.getHoraInicio(),
                reservaExistente.getHoraFin()
        ));

        return reservaRepository.save(reservaExistente);
    }

    private void actualizarClienteSiEsNecesario(ReservaEntity reservaExistente, ReservaEntity reservaActualizada) {
        if (reservaActualizada.getCliente() != null &&
                !reservaExistente.getCliente().getRut().equals(reservaActualizada.getCliente().getRut())) {

            Optional<ClienteEntity> clienteOpt = clienteRepository.findById(reservaActualizada.getCliente().getRut());
            if (clienteOpt.isEmpty()) {
                throw new ClienteNoEncontradoException("Cliente no encontrado");
            }
            reservaExistente.setCliente(clienteOpt.get());
        }
    }

    private void validarHorariosActualizacion(ReservaEntity reservaActualizada, LocalDate diaReserva,
                                              LocalTime horaInicio, LocalTime horaFin, Long idReserva) {
        if (reservaActualizada.getDiaReserva() != null ||
                reservaActualizada.getHoraInicio() != null ||
                reservaActualizada.getHoraFin() != null) {

            // Validar que las horas sean válidas
            if (horaInicio.isAfter(horaFin) || horaInicio.equals(horaFin)) {
                throw new HorarioInvalidoException("La hora de inicio debe ser anterior a la hora de fin");
            }

            // Validar disponibilidad de horario (excluyendo la reserva actual)
            if (!validarDisponibilidadHorario(diaReserva, horaInicio, horaFin, idReserva)) {
                throw new HorarioNoDisponibleException("El horario seleccionado no está disponible. Ya existe una reserva que se superpone con este horario.");
            }
        }
    }

    private void actualizarCamposReserva(ReservaEntity reservaExistente, ReservaEntity reservaActualizada) {
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
    }

    // NUEVO MÉTODO: Obtener horarios disponibles para un día específico
    public List<String> obtenerHorariosDisponibles(LocalDate diaReserva, int duracionMinutos) {
        List<String> horariosDisponibles = new java.util.ArrayList<>();

        // Definir horario de funcionamiento (puedes ajustar según tu negocio)
        LocalTime horaApertura = LocalTime.of(9, 0); // 9:00 AM
        LocalTime horaCierre = LocalTime.of(22, 0);  // 10:00 PM

        // Generar slots de tiempo cada 30 minutos
        LocalTime horaActual = horaApertura;
        while (horaActual.plusMinutes(duracionMinutos).isBefore(horaCierre) ||
                horaActual.plusMinutes(duracionMinutos).equals(horaCierre)) {

            LocalTime horaFin = horaActual.plusMinutes(duracionMinutos);

            if (validarDisponibilidadHorario(diaReserva, horaActual, horaFin)) {
                horariosDisponibles.add(horaActual.toString() + " - " + horaFin.toString());
            }

            horaActual = horaActual.plusMinutes(30); // Incrementar cada 30 minutos
        }

        return horariosDisponibles;
    }

    // DTO para calendario con campos privados y getters/setters
    public static class ReservaDTO {
        private Long id;
        private String cliente;
        private String start;
        private String end;
        private String title;

        public ReservaDTO(Long id, String cliente, String start, String end, String title) {
            this.id = id;
            this.cliente = cliente;
            this.start = start;
            this.end = end;
            this.title = title;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getCliente() {
            return cliente;
        }

        public void setCliente(String cliente) {
            this.cliente = cliente;
        }

        public String getStart() {
            return start;
        }

        public void setStart(String start) {
            this.start = start;
        }

        public String getEnd() {
            return end;
        }

        public void setEnd(String end) {
            this.end = end;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
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
        }).toList();
    }

    // Eliminar una reserva
    public void eliminarReserva(Long idReserva) {
        Optional<ReservaEntity> reservaOpt = reservaRepository.findById(idReserva);
        if (reservaOpt.isPresent()) {
            // Quitar vínculo con comprobante (si existe)
            desvincularComprobante(idReserva);

            // Ahora puedes eliminar la reserva sin que explote
            reservaRepository.deleteById(idReserva);
        }
    }

    private void desvincularComprobante(Long idReserva) {
        try {
            comprobantePagoService.getComprobanteByReservaId(idReserva).ifPresent(comprobante ->
                    comprobante.setReserva(null) // Rompe la relación
            );
        } catch (Exception e) {
            throw new ReservaException("Error al desvincular comprobante de la reserva", e);
        }
    }

    // NUEVO MÉTODO: Generar comprobantes para reservas existentes sin comprobante
    public void generarComprobantesFaltantes() {
        try {
            List<ReservaEntity> todasLasReservas = reservaRepository.findAll();
            int comprobantesGenerados = 0;

            for (ReservaEntity reserva : todasLasReservas) {
                comprobantesGenerados += procesarReservaParaComprobante(reserva);
            }

            logger.info("Proceso completado. Comprobantes generados: {}", comprobantesGenerados);

        } catch (Exception e) {
            throw new ReservaException("Error generando comprobantes faltantes", e);
        }
    }

    private int procesarReservaParaComprobante(ReservaEntity reserva) {
        try {
            // Verificar si ya existe un comprobante para esta reserva
            Optional<com.example.demo.entities.ComprobantePagoEntity> comprobanteExistente =
                    comprobantePagoService.getComprobanteByReservaId(reserva.getIdReserva());

            if (comprobanteExistente.isEmpty()) {
                // Generar comprobante faltante
                comprobantePagoService.generarComprobantePago(reserva.getIdReserva());
                logger.info("Comprobante generado para reserva: {}", reserva.getIdReserva());
                return 1;
            }
            return 0;
        } catch (Exception e) {
            throw new ReservaException("Error al generar comprobante para reserva: " + reserva.getIdReserva(), e);
        }
    }
}