package com.example.demo;

import com.example.demo.entities.*;
import com.example.demo.repositories.ComprobantePagoRepository;
import com.example.demo.repositories.TarifaRepository;
import com.example.demo.service.ClienteService;
import com.example.demo.service.ComprobantePagoService;
import com.example.demo.service.ReservaService;
import com.example.demo.service.TarifaService;
import org.springframework.mail.javamail.JavaMailSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ComprobantePagoServiceTest {

    @Mock
    private ComprobantePagoRepository comprobantePagoRepository;

    @Mock
    private ClienteService clienteService;

    @Mock
    private ReservaService reservaService;

    @Mock
    private TarifaService tarifaService;

    @Mock
    private TarifaRepository tarifaRepository;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private ComprobantePagoService comprobantePagoService;

    private ClienteEntity cliente;
    private ReservaEntity reserva;
    private TarifaEntity tarifa;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        cliente = new ClienteEntity();
        cliente.setRut("20401575-9");
        cliente.setNombre("joaquin");
        cliente.setEmail("joaking.alambritox@gmail.com");
        cliente.setDescuentoAplicable(20);
        cliente.setFechaCumple(LocalDate.of(2000, 2, 4));

        tarifa = new TarifaEntity();
        tarifa.setId(1L);
        tarifa.setNumeroVueltas(10);
        tarifa.setTiempoMaximo(15);
        tarifa.setPrecioBase(10000);
        tarifa.setDuracionReserva(20);

        reserva = new ReservaEntity();
        reserva.setIdReserva(1L);
        reserva.setCliente(cliente);
        reserva.setDiaReserva(LocalDate.of(2025, 2, 4)); // coincide con cumpleaños
        reserva.setHoraInicio(LocalTime.of(10, 0));
        reserva.setHoraFin(LocalTime.of(11, 0));
        reserva.setTiempoReserva(20);
        reserva.setAcompanantes(List.of("21556446-0"));
    }

    @Test
    void testGenerarComprobantePago_Success() {
        ClienteEntity acompanante = new ClienteEntity();
        acompanante.setRut("21556446-0");
        acompanante.setNombre("belen");
        acompanante.setDescuentoAplicable(0);
        acompanante.setFechaCumple(LocalDate.of(2004, 4, 20));

        when(reservaService.obtenerReservaPorId(1L)).thenReturn(Optional.of(reserva));
        when(tarifaService.getTarifaById(anyLong())).thenReturn(Optional.of(tarifa));
        when(tarifaRepository.findFirstByTiempoMaximoGreaterThanEqualOrderByTiempoMaximoAsc(anyInt())).thenReturn(tarifa);
        when(clienteService.getClienteById("21556446-0")).thenReturn(Optional.of(acompanante));
        when(comprobantePagoRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        ComprobantePagoEntity comprobante = comprobantePagoService.generarComprobantePago(1L);

        assertNotNull(comprobante);
        assertEquals(cliente.getRut(), comprobante.getCliente().getRut());
        assertEquals(1 + reserva.getAcompanantes().size(), // total personas
                comprobante.getReserva().getAcompanantes().size() + 1);
        assertTrue(comprobante.getMontoTotalConIva().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testGenerarComprobantePago_ReservaNoExiste() {
        when(reservaService.obtenerReservaPorId(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                comprobantePagoService.generarComprobantePago(999L));
        assertTrue(ex.getMessage().contains("No se encontró la reserva"));
    }

    @Test
    void testGetComprobanteById() {
        ComprobantePagoEntity comprobante = new ComprobantePagoEntity();
        comprobante.setId(5L);
        when(comprobantePagoRepository.findById(5L)).thenReturn(Optional.of(comprobante));

        Optional<ComprobantePagoEntity> result = comprobantePagoService.getComprobanteById(5L);
        assertTrue(result.isPresent());
        assertEquals(5L, result.get().getId());
    }

    @Test
    void testGetComprobanteByReservaId() {
        ComprobantePagoEntity comprobante = new ComprobantePagoEntity();
        when(comprobantePagoRepository.findByReservaIdReserva(1L)).thenReturn(Optional.of(comprobante));
        Optional<ComprobantePagoEntity> result = comprobantePagoService.getComprobanteByReservaId(1L);
        assertTrue(result.isPresent());
    }

    @Test
    void testGetAllComprobantes() {
        when(comprobantePagoRepository.findAll()).thenReturn(List.of(new ComprobantePagoEntity()));
        List<ComprobantePagoEntity> result = comprobantePagoService.getAllComprobantes();
        assertEquals(1, result.size());
    }
}
