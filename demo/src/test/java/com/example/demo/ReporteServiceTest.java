package com.example.demo;

import com.example.demo.entities.ReporteEntity;
import com.example.demo.repositories.ReporteRepository;

import com.example.demo.service.ReporteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReporteServiceTest {

    @Mock
    private ReporteRepository reporteRepository;

    @InjectMocks
    private ReporteService reporteService;

    private ReporteEntity reporte;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

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
        when(reporteRepository.save(reporte)).thenReturn(reporte);
        ReporteEntity guardado = reporteService.guardarReporte(reporte);
        assertNotNull(guardado);
        assertEquals("PorVueltas", guardado.getTipoReporte());
    }

    @Test
    void testObtenerTodos() {
        when(reporteRepository.findAll()).thenReturn(List.of(reporte));
        List<ReporteEntity> lista = reporteService.obtenerTodos();
        assertEquals(1, lista.size());
        assertEquals("PorVueltas", lista.get(0).getTipoReporte());
    }

    @Test
    void testObtenerPorTipo() {
        when(reporteRepository.findByTipoReporte("PorVueltas")).thenReturn(List.of(reporte));
        List<ReporteEntity> lista = reporteService.obtenerPorTipo("PorVueltas");
        assertEquals(1, lista.size());
        assertEquals(10, lista.get(0).getNumeroVueltas());
    }

    @Test
    void testObtenerPorMes() {
        LocalDate mes = LocalDate.of(2025, 4, 1);
        when(reporteRepository.findByMesGenerado(mes)).thenReturn(List.of(reporte));
        List<ReporteEntity> lista = reporteService.obtenerPorMes(mes);
        assertEquals(1, lista.size());
        assertEquals(15, lista.get(0).getTiempoMaximo());
    }

    @Test
    void testObtenerPorId() {
        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporte));
        Optional<ReporteEntity> result = reporteService.obtenerPorId(1L);
        assertTrue(result.isPresent());
        assertEquals(50000.0, result.get().getIngresoTotal());
    }

    @Test
    void testEliminarReporte() {
        reporteService.eliminarReporte(1L);
        verify(reporteRepository).deleteById(1L);
    }
}
