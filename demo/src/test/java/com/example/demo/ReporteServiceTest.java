package com.example.demo;

import com.example.demo.entities.ReporteEntity;
import com.example.demo.entities.ReporteMensualEntity;
import com.example.demo.repositories.ReporteMensualRepository;
import com.example.demo.repositories.ReporteRepository;
import com.example.demo.service.ReporteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReporteServiceTest {

    @Mock
    private ReporteRepository reporteRepository;

    @Mock
    private ReporteMensualRepository reporteMensualRepository;

    @InjectMocks
    private ReporteService reporteService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGuardarReporte() {
        ReporteEntity reporte = new ReporteEntity();
        reporte.setId(1L);
        reporte.setFecha(LocalDate.now());
        reporte.setTipoReporte("PorVueltas");
        reporte.setIngresoTotal(10000.0);
        reporte.setNumeroVueltas(50);

        when(reporteRepository.save(reporte)).thenReturn(reporte);
        when(reporteMensualRepository.findByTipoReporteAndMesAndAnio(anyString(), anyInt(), anyInt()))
                .thenReturn(Optional.empty());

        ReporteEntity result = reporteService.guardarReporte(reporte);

        assertEquals(reporte, result);
        verify(reporteRepository).save(reporte);
        verify(reporteMensualRepository).save(any(ReporteMensualEntity.class));
    }


    @Test
    void testEliminarReporte_NoExistente_NoExcepcion() {
        when(reporteRepository.findById(1L)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> reporteService.eliminarReporte(1L));
        verify(reporteRepository, never()).deleteById(anyLong());
    }

    @Test
    void testActualizarReporteMensualAgregar_PorVueltas() {
        ReporteEntity reporte = new ReporteEntity();
        reporte.setFecha(LocalDate.now());
        reporte.setTipoReporte("PorVueltas");
        reporte.setIngresoTotal(8000.0);
        reporte.setNumeroVueltas(40);

        when(reporteMensualRepository.findByTipoReporteAndMesAndAnio(anyString(), anyInt(), anyInt()))
                .thenReturn(Optional.empty());

        reporteService.guardarReporte(reporte);

        ArgumentCaptor<ReporteMensualEntity> captor = ArgumentCaptor.forClass(ReporteMensualEntity.class);
        verify(reporteMensualRepository).save(captor.capture());

        ReporteMensualEntity savedReporteMensual = captor.getValue();
        assertEquals(8000.0, savedReporteMensual.getIngresoTotal());
        assertEquals(40, savedReporteMensual.getTotalVueltas());
        assertEquals(1, savedReporteMensual.getCantidadReportes());
    }

    @Test
    void testActualizarReporteMensualAgregar_PorPersonas() {
        ReporteEntity reporte = new ReporteEntity();
        reporte.setFecha(LocalDate.now());
        reporte.setTipoReporte("PorPersonas");
        reporte.setIngresoTotal(12000.0);
        reporte.setMinPersonas(2);
        reporte.setMaxPersonas(4);

        when(reporteMensualRepository.findByTipoReporteAndMesAndAnio(anyString(), anyInt(), anyInt()))
                .thenReturn(Optional.empty());

        reporteService.guardarReporte(reporte);

        ArgumentCaptor<ReporteMensualEntity> captor = ArgumentCaptor.forClass(ReporteMensualEntity.class);
        verify(reporteMensualRepository).save(captor.capture());

        ReporteMensualEntity savedReporteMensual = captor.getValue();
        assertEquals(12000.0, savedReporteMensual.getIngresoTotal());
        assertEquals(2, savedReporteMensual.getTotalPersonasMin());
        assertEquals(4, savedReporteMensual.getTotalPersonasMax());
        assertEquals(1, savedReporteMensual.getCantidadReportes());
    }
}