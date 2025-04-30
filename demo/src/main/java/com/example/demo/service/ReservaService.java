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

    // Crear una nueva reserva
    public ReservaEntity crearReserva(ReservaEntity reserva) {
        // Verificar que el cliente existe
        Optional<ClienteEntity> clienteOpt = clienteRepository.findById(reserva.getCliente().getRut());
        if (clienteOpt.isEmpty()) {
            throw new RuntimeException("Cliente no encontrado");
        }
        return reservaRepository.save(reserva);
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

        return reservaRepository.save(reservaExistente);
    }

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
}