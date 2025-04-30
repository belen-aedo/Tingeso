package com.example.demo.service;

import com.example.demo.entities.ClienteEntity;
import com.example.demo.entities.ComprobantePagoEntity;
import com.example.demo.entities.ReservaEntity;
import com.example.demo.entities.TarifaEntity;
import com.example.demo.repositories.ComprobantePagoRepository;
import com.example.demo.repositories.ReservaRepository;
import com.example.demo.repositories.TarifaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class ComprobantePagoServiceTest {

    @Autowired
    private ComprobantePagoService comprobantePagoService;

    @Autowired
    private ComprobantePagoRepository comprobantePagoRepository;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private TarifaService tarifaService;

    @Autowired
    private TarifaRepository tarifaRepository;

    private ClienteEntity cliente;
    private ReservaEntity reserva;
    private TarifaEntity tarifa;

    @BeforeEach
    void setup() {
        comprobantePagoRepository.deleteAll();
        reservaRepository.deleteAll();
        tarifaRepository.deleteAll();

        cliente = new ClienteEntity();
        cliente.setRut("12345678-9");
        cliente.setNombre("Belén Aedo");
        cliente.setEmail("belen.aedo@usach.cl");
        cliente.setFechaCumple(LocalDate.of(1995, 4, 30));
        cliente.setDescuentoAplicable(10);
        clienteService.saveCliente(cliente);

        tarifa = new TarifaEntity();
        tarifa.setNumeroVueltas(10);
        tarifa.setTiempoMaximo(15);
        tarifa.setPrecioBase(5000);
        tarifa.setDuracionReserva(30);
        tarifa = tarifaRepository.save(tarifa);

        reserva = new ReservaEntity();
        reserva.setCliente(cliente);
        reserva.setDiaReserva(LocalDate.now());
        reserva.setHoraInicio(LocalTime.of(14, 0));
        reserva.setHoraFin(LocalTime.of(15, 0));
        reserva.setTiempoReserva(30);
        reserva = reservaRepository.save(reserva);
    }

    @Test
    void whenGenerarComprobantePago_thenSuccess() {
        ComprobantePagoEntity comprobante = comprobantePagoService.generarComprobantePago(reserva.getIdReserva());
        assertThat(comprobante.getCliente().getRut()).isEqualTo("12345678-9");
        assertThat(comprobante.getReserva().getIdReserva()).isEqualTo(reserva.getIdReserva());
        assertThat(comprobante.getMontoTotalConIva()).isGreaterThanOrEqualTo(BigDecimal.valueOf(0));
    }

    @Test
    void whenGetAllComprobantes_thenReturnList() {
        comprobantePagoService.generarComprobantePago(reserva.getIdReserva());
        List<ComprobantePagoEntity> lista = comprobantePagoService.getAllComprobantes();
        assertThat(lista).isNotEmpty();
    }

    @Test
    void whenGetComprobanteById_thenReturnCorrectOne() {
        ComprobantePagoEntity comprobante = comprobantePagoService.generarComprobantePago(reserva.getIdReserva());
        Optional<ComprobantePagoEntity> found = comprobantePagoService.getComprobanteById(comprobante.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getReserva().getIdReserva()).isEqualTo(reserva.getIdReserva());
    }

    @Test
    void whenGetComprobanteByReservaId_thenReturnCorrectOne() {
        ComprobantePagoEntity comprobante = comprobantePagoService.generarComprobantePago(reserva.getIdReserva());
        Optional<ComprobantePagoEntity> found = comprobantePagoService.getComprobanteByReservaId(reserva.getIdReserva());
        assertThat(found).isPresent();
    }

    @Test
    void whenGetComprobantesByCliente_thenReturnList() {
        comprobantePagoService.generarComprobantePago(reserva.getIdReserva());
        List<ComprobantePagoEntity> lista = comprobantePagoService.getComprobantesByCliente(cliente);
        assertThat(lista).isNotEmpty();
    }

    // Nota: No se incluye el test para envio de email real para evitar efectos secundarios.
}
