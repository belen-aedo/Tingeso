package com.example.demo;

import com.example.demo.entities.ReporteEntity;
import com.example.demo.repositories.ReporteRepository;
import com.example.demo.service.ReporteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class ReporteServiceTest {

    @Autowired
    private ReporteService reporteService;

    @Autowired
    private ReporteRepository reporteRepository;

    private ReporteEntity reporte;

    @BeforeEach
    void setup() {
        reporteRepository.deleteAll();
        reporte = new ReporteEntity();
        reporte.setTipoReporte("PorVueltas");
        reporte.setMesGenerado(LocalDate.of(2024, 4, 1));
        reporte.setNumeroVueltas(10);
        reporte.setTiempoMaximo(15);
        reporte.setIngresoTotal(10000.0);
        reporteRepository.save(reporte);
    }

    @Test
    void whenGuardarReporte_thenItIsSaved() {
        ReporteEntity nuevo = new ReporteEntity();
        nuevo.setTipoReporte("PorPersonas");
        nuevo.setMesGenerado(LocalDate.of(2024, 4, 1));
        nuevo.setMinPersonas(3);
        nuevo.setMaxPersonas(5);
        nuevo.setIngresoTotal(20000.0);

        ReporteEntity saved = reporteService.guardarReporte(nuevo);
        assertThat(saved.getTipoReporte()).isEqualTo("PorPersonas");
        assertThat(saved.getIngresoTotal()).isEqualTo(20000.0);
    }

    @Test
    void whenObtenerTodos_thenReturnList() {
        List<ReporteEntity> reportes = reporteService.obtenerTodos();
        assertThat(reportes).isNotEmpty();
    }

    @Test
    void whenObtenerPorTipo_thenReturnFiltered() {
        List<ReporteEntity> reportes = reporteService.obtenerPorTipo("PorVueltas");
        assertThat(reportes).hasSize(1);
        assertThat(reportes.get(0).getNumeroVueltas()).isEqualTo(10);
    }

    @Test
    void whenObtenerPorMes_thenReturnFiltered() {
        List<ReporteEntity> reportes = reporteService.obtenerPorMes(LocalDate.of(2024, 4, 1));
        assertThat(reportes).hasSize(1);
    }

    @Test
    void whenObtenerPorId_thenReturnOptional() {
        Optional<ReporteEntity> found = reporteService.obtenerPorId(reporte.getId());
        assertThat(found).isPresent();
    }

    @Test
    void whenEliminarReporte_thenItIsDeleted() {
        reporteService.eliminarReporte(reporte.getId());
        Optional<ReporteEntity> deleted = reporteRepository.findById(reporte.getId());
        assertThat(deleted).isEmpty();
    }
}
