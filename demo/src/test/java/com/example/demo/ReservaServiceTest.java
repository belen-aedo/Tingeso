package com.example.demo;

import com.example.demo.entities.ClienteEntity;
import com.example.demo.entities.ReservaEntity;
import com.example.demo.repositories.ClienteRepository;
import com.example.demo.repositories.ReservaRepository;
import com.example.demo.service.ReservaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
public class ReservaServiceTest {

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    private ClienteEntity cliente;
    private ReservaEntity reserva;

    @BeforeEach
    void setup() {
        reservaRepository.deleteAll();
        clienteRepository.deleteAll();

        cliente = new ClienteEntity();
        cliente.setRut("11111111-1");
        cliente.setNombre("Pedro Perez");
        cliente.setEmail("pedro@example.com");
        clienteRepository.save(cliente);

        reserva = new ReservaEntity();
        reserva.setCliente(cliente);
        reserva.setDiaReserva(LocalDate.of(2025, 4, 30));
        reserva.setHoraInicio(LocalTime.of(10, 0));
        reserva.setHoraFin(LocalTime.of(11, 0));
        reservaRepository.save(reserva);
    }

    @Test
    void whenObtenerTodasLasReservas_thenReturnList() {
        List<ReservaEntity> reservas = reservaService.obtenerTodasLasReservas();
        assertThat(reservas).isNotEmpty();
    }

    @Test
    void whenObtenerReservaPorId_thenReturnOptional() {
        Optional<ReservaEntity> found = reservaService.obtenerReservaPorId(reserva.getIdReserva());
        assertThat(found).isPresent();
    }

    @Test
    void whenObtenerReservasPorDia_thenReturnList() {
        List<ReservaEntity> reservas = reservaService.obtenerReservasPorDia(LocalDate.of(2025, 4, 30));
        assertThat(reservas).hasSize(1);
    }

    @Test
    void whenObtenerReservasPorCliente_thenReturnList() {
        List<ReservaEntity> reservas = reservaService.obtenerReservasPorCliente("11111111-1");
        assertThat(reservas).hasSize(1);
    }

    @Test
    void whenCrearReservaWithExistingCliente_thenSuccess() {
        ReservaEntity nueva = new ReservaEntity();
        nueva.setCliente(cliente);
        nueva.setDiaReserva(LocalDate.now());
        nueva.setHoraInicio(LocalTime.of(14, 0));
        nueva.setHoraFin(LocalTime.of(15, 0));

        ReservaEntity saved = reservaService.crearReserva(nueva);
        assertThat(saved.getCliente().getRut()).isEqualTo("11111111-1");
    }

    @Test
    void whenCrearReservaWithNonExistingCliente_thenThrowException() {
        ClienteEntity falso = new ClienteEntity();
        falso.setRut("00000000-0");

        ReservaEntity nueva = new ReservaEntity();
        nueva.setCliente(falso);

        assertThatThrownBy(() -> reservaService.crearReserva(nueva))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cliente no encontrado");
    }

    @Test
    void whenActualizarReserva_thenSuccess() {
        reserva.setAcompanantes(List.of("22222222-2"));
        ReservaEntity actualizada = reservaService.actualizarReserva(reserva.getIdReserva(), reserva);
        assertThat(actualizada.getAcompanantes()).contains("22222222-2");
    }

    @Test
    void whenActualizarReservaInexistente_thenThrowException() {
        ReservaEntity nueva = new ReservaEntity();
        assertThatThrownBy(() -> reservaService.actualizarReserva(999L, nueva))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Reserva no encontrada");
    }

    @Test
    void whenObtenerReservasDTO_thenReturnFormattedList() {
        List<ReservaService.ReservaDTO> dtos = reservaService.obtenerReservasDTO();
        assertThat(dtos).hasSize(1);
        assertThat(dtos.get(0).title).contains("Cliente: Pedro Perez");
    }

    @Test
    void whenEliminarReserva_thenRemoveIt() {
        reservaService.eliminarReserva(reserva.getIdReserva());
        Optional<ReservaEntity> deleted = reservaRepository.findById(reserva.getIdReserva());
        assertThat(deleted).isEmpty();
    }
}
