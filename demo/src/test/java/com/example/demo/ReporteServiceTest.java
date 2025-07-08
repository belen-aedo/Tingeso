package com.example.demo;

import com.example.demo.entities.ReporteEntity;
import com.example.demo.repositories.ReporteRepository;
import com.example.demo.service.ReporteService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock
    private ReporteRepository reporteRepository;

    @InjectMocks
    private ReporteService reporteService;

    private ReporteEntity reporte;

    @BeforeEach
    void setUp() {
        reporte = new ReporteEntity();
        reporte.setId(1L);
        reporte.setMesGenerado(LocalDate.of(2025, 4, 1));
        reporte.setTipoReporte("PorVueltas");
        reporte.setNumeroVueltas(10);
        reporte.setTiempoMaximo(15);
        reporte.setIngresoTotal(50000.0);
    }

    @Test
    void testGuardarReporte() {
        // Arrange
        when(reporteRepository.save(reporte)).thenReturn(reporte);

        // Act
        ReporteEntity result = reporteService.guardarReporte(reporte);

        // Assert
        assertNotNull(result);
        assertEquals("PorVueltas", result.getTipoReporte());
        verify(reporteRepository, times(1)).save(reporte);
    }

    @Test
    void testObtenerTodos() {
        when(reporteRepository.findAll()).thenReturn(List.of(reporte));

        List<ReporteEntity> lista = reporteService.obtenerTodos();

        assertEquals(1, lista.size());
        assertEquals("PorVueltas", lista.get(0).getTipoReporte());
        verify(reporteRepository, times(1)).findAll();
    }

    @Test
    void testObtenerPorTipo() {
        when(reporteRepository.findByTipoReporte("PorVueltas")).thenReturn(List.of(reporte));

        List<ReporteEntity> lista = reporteService.obtenerPorTipo("PorVueltas");

        assertEquals(1, lista.size());
        assertEquals(10, lista.get(0).getNumeroVueltas());
        verify(reporteRepository, times(1)).findByTipoReporte("PorVueltas");
    }

    @Test
    void testObtenerPorMes() {
        LocalDate mes = LocalDate.of(2025, 4, 1);
        when(reporteRepository.findByMesGenerado(mes)).thenReturn(List.of(reporte));

        List<ReporteEntity> lista = reporteService.obtenerPorMes(mes);

        assertEquals(1, lista.size());
        assertEquals(15, lista.get(0).getTiempoMaximo());
        verify(reporteRepository, times(1)).findByMesGenerado(mes);
    }

    @Test
    void testObtenerPorId() {
        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporte));

        Optional<ReporteEntity> result = reporteService.obtenerPorId(1L);

        assertTrue(result.isPresent());
        assertEquals(50000.0, result.get().getIngresoTotal());
        verify(reporteRepository, times(1)).findById(1L);
    }

    @Test
    void testEliminarReporte() {
        doNothing().when(reporteRepository).deleteById(1L);

        reporteService.eliminarReporte(1L);

        verify(reporteRepository, times(1)).deleteById(1L);
    }
}
