package com.example.demo;

import com.example.demo.entities.ClienteEntity;
import com.example.demo.entities.ReservaEntity;
import com.example.demo.repositories.ClienteRepository;
import com.example.demo.repositories.ReservaRepository;

import com.example.demo.service.ReservaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ReservaService reservaService;

    private ClienteEntity cliente;
    private ReservaEntity reserva;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        cliente = new ClienteEntity();
        cliente.setRut("20401575-9");
        cliente.setNombre("joaquin");
        cliente.setEmail("joaking.alambritox@gmail.com");

        reserva = new ReservaEntity();
        reserva.setIdReserva(1L);
        reserva.setCliente(cliente);
        reserva.setDiaReserva(LocalDate.of(2025, 5, 10));
        reserva.setHoraInicio(LocalTime.of(10, 0));
        reserva.setHoraFin(LocalTime.of(11, 0));
        reserva.setAcompanantes(new ArrayList<>());
    }

    @Test
    void testObtenerTodasLasReservas() {
        when(reservaRepository.findAll()).thenReturn(List.of(reserva));
        List<ReservaEntity> reservas = reservaService.obtenerTodasLasReservas();
        assertEquals(1, reservas.size());
        assertEquals(cliente.getNombre(), reservas.get(0).getCliente().getNombre());
    }

    @Test
    void testObtenerReservaPorId() {
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        Optional<ReservaEntity> result = reservaService.obtenerReservaPorId(1L);
        assertTrue(result.isPresent());
        assertEquals("joaquin", result.get().getCliente().getNombre());
    }

    @Test
    void testObtenerReservasPorDia() {
        LocalDate dia = LocalDate.of(2025, 5, 10);
        when(reservaRepository.findByDiaReserva(dia)).thenReturn(List.of(reserva));
        List<ReservaEntity> reservas = reservaService.obtenerReservasPorDia(dia);
        assertEquals(1, reservas.size());
    }

    @Test
    void testCrearReservaConClienteExistente() {
        when(clienteRepository.findById("20401575-9")).thenReturn(Optional.of(cliente));
        when(reservaRepository.save(reserva)).thenReturn(reserva);

        ReservaEntity creada = reservaService.crearReserva(reserva);
        assertEquals("joaquin", creada.getCliente().getNombre());
    }

    @Test
    void testCrearReservaConClienteInexistente() {
        when(clienteRepository.findById("20401575-9")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            reservaService.crearReserva(reserva);
        });
        assertEquals("Cliente no encontrado", ex.getMessage());
    }

    @Test
    void testActualizarReserva() {
        ReservaEntity actualizada = new ReservaEntity();
        actualizada.setCliente(cliente);
        actualizada.setDiaReserva(LocalDate.of(2025, 5, 12));
        actualizada.setHoraInicio(LocalTime.of(12, 0));
        actualizada.setHoraFin(LocalTime.of(13, 0));

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(clienteRepository.findById("20401575-9")).thenReturn(Optional.of(cliente));
        when(reservaRepository.save(any())).thenReturn(reserva);

        ReservaEntity result = reservaService.actualizarReserva(1L, actualizada);
        assertEquals(LocalDate.of(2025, 5, 12), result.getDiaReserva());
    }

    @Test
    void testEliminarReserva() {
        reservaService.eliminarReserva(1L);
        verify(reservaRepository).deleteById(1L);
    }
}
