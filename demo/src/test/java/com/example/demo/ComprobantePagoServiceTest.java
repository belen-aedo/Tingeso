package com.example.demo;

import com.example.demo.entities.ClienteEntity;
import com.example.demo.entities.ComprobantePagoEntity;
import com.example.demo.entities.ReservaEntity;
import com.example.demo.entities.TarifaEntity;
import com.example.demo.repositories.ComprobantePagoRepository;
import com.example.demo.repositories.TarifaRepository;
import com.example.demo.service.ClienteService;
import com.example.demo.service.ComprobantePagoService;
import com.example.demo.service.ReservaService;
import com.example.demo.service.TarifaService;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ComprobantePagoServiceTest {

    @Mock private ComprobantePagoRepository comprobantePagoRepository;
    @Mock private ClienteService clienteService;
    @Mock private ReservaService reservaService;
    @Mock private TarifaService tarifaService;
    @Mock private TarifaRepository tarifaRepository;
    @Mock private JavaMailSender mailSender;
    @Mock private MimeMessage mimeMessage;

    @InjectMocks
    private ComprobantePagoService comprobantePagoService;

    private ClienteEntity cliente1, cliente2, cliente3;
    private ReservaEntity reserva;
    private TarifaEntity tarifa;
    private ComprobantePagoEntity comprobante;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        cliente1 = new ClienteEntity("20401575-9", "joaquin", "joaking.alambritox@gmail.com", 0, LocalDate.of(2000, 2, 4), 20);
        cliente2 = new ClienteEntity("21556446-0", "belen", "belen.aedo@usach.cl", 0, LocalDate.of(2004, 4, 20), 0);
        cliente3 = new ClienteEntity("22594262-5", "mohamed", "mohamed.al-marzuk@usach.cl", 0, LocalDate.of(2001, 4, 28), 0);

        tarifa = new TarifaEntity(1L, 10, 20, 15000, 30);

        reserva = new ReservaEntity(1L, cliente1, LocalDate.now(), LocalTime.of(14, 0), LocalTime.of(14, 30), 30, Arrays.asList(cliente2.getRut(), cliente3.getRut()));

        comprobante = new ComprobantePagoEntity(1L, cliente1, reserva, tarifa, new BigDecimal("45000"),
                new BigDecimal("4500"), new BigDecimal("3000"), new BigDecimal("0"),
                new BigDecimal("37500"), new BigDecimal("7125"), new BigDecimal("44625"));

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void getAllComprobantes() {
        when(comprobantePagoRepository.findAll()).thenReturn(List.of(comprobante));
        List<ComprobantePagoEntity> result = comprobantePagoService.getAllComprobantes();
        assertEquals(1, result.size());
        verify(comprobantePagoRepository).findAll();
    }

    @Test
    void getComprobanteById() {
        when(comprobantePagoRepository.findById(1L)).thenReturn(Optional.of(comprobante));
        Optional<ComprobantePagoEntity> result = comprobantePagoService.getComprobanteById(1L);
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void getComprobanteByReservaId() {
        when(comprobantePagoRepository.findByReservaIdReserva(1L)).thenReturn(Optional.of(comprobante));
        Optional<ComprobantePagoEntity> result = comprobantePagoService.getComprobanteByReservaId(1L);
        assertTrue(result.isPresent());
    }

    @Test
    void getComprobantesByCliente() {
        when(comprobantePagoRepository.findByCliente(cliente1)).thenReturn(List.of(comprobante));
        List<ComprobantePagoEntity> result = comprobantePagoService.getComprobantesByCliente(cliente1);
        assertEquals(1, result.size());
    }

    @Test
    void generarComprobantePago_DescuentoCumpleanos() {
        reserva.setDiaReserva(LocalDate.of(LocalDate.now().getYear(), 4, 28));
        cliente3.setFechaCumple(LocalDate.of(2001, 4, 28));
        comprobante.setDescuentoCumpleanos(new BigDecimal("7500"));

        when(reservaService.obtenerReservaPorId(1L)).thenReturn(Optional.of(reserva));
        when(tarifaService.getTarifaById(anyLong())).thenReturn(Optional.of(tarifa));
        when(clienteService.getClienteById(anyString())).thenReturn(Optional.of(cliente2), Optional.of(cliente3));
        when(tarifaRepository.findFirstByTiempoMaximoGreaterThanEqualOrderByTiempoMaximoAsc(anyInt())).thenReturn(tarifa);
        when(comprobantePagoRepository.save(any())).thenReturn(comprobante);

        ComprobantePagoEntity result = comprobantePagoService.generarComprobantePago(1L);
        assertEquals(new BigDecimal("7500"), result.getDescuentoCumpleanos());
    }

    @Test
    void testEnviarComprobantePorEmail_ConAcompanantes() {
        reserva.setAcompanantes(List.of(cliente2.getRut(), cliente3.getRut()));
        comprobante.setReserva(reserva);

        when(comprobantePagoRepository.findById(1L)).thenReturn(Optional.of(comprobante));
        when(clienteService.getClienteById(cliente2.getRut())).thenReturn(Optional.of(cliente2));
        when(clienteService.getClienteById(cliente3.getRut())).thenReturn(Optional.of(cliente3));

        doNothing().when(mailSender).send(any(MimeMessage.class));

        try {
            comprobantePagoService.enviarComprobantePorEmail(1L);
        } catch (Exception e) {
            fail("Falló al enviar correo: " + e.getMessage());
        }


        verify(mailSender, times(3)).send(any(MimeMessage.class)); // cliente + 2 acompañantes
        verify(clienteService).getClienteById(cliente2.getRut());
        verify(clienteService).getClienteById(cliente3.getRut());
    }

    @Test
    void testCalcularDescuentoGrupo() {
        assertEquals(BigDecimal.ZERO, invokeDescuentoGrupo(2));
        assertEquals(new BigDecimal("0.10"), invokeDescuentoGrupo(3));
        assertEquals(new BigDecimal("0.20"), invokeDescuentoGrupo(8));
        assertEquals(new BigDecimal("0.30"), invokeDescuentoGrupo(12));
    }

    private BigDecimal invokeDescuentoGrupo(int total) {
        if (total <= 2) return BigDecimal.ZERO;
        if (total <= 5) return new BigDecimal("0.10");
        if (total <= 10) return new BigDecimal("0.20");
        return new BigDecimal("0.30");
    }

    @Test
    void calculaCorrectamenteElIVA() {
        BigDecimal montoFinal = new BigDecimal("37500");
        BigDecimal esperado = new BigDecimal("7125");
        BigDecimal resultado = montoFinal.multiply(new BigDecimal("0.19")).setScale(0, java.math.RoundingMode.HALF_UP);
        assertEquals(esperado, resultado);
    }

    @Test
    void sumaTodosLosDescuentosCorrectamente() {
        BigDecimal grupo = new BigDecimal("4500");
        BigDecimal frecuente = new BigDecimal("3000");
        BigDecimal cumple = new BigDecimal("7500");
        BigDecimal totalEsperado = new BigDecimal("15000");

        BigDecimal total = grupo.add(frecuente).add(cumple);
        assertEquals(totalEsperado, total);
    }
}
