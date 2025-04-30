package com.example.demo;

import com.example.demo.entities.TarifaEntity;
import com.example.demo.repositories.TarifaRepository;
import com.example.demo.service.TarifaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TarifaServiceTest {

    @Mock
    private TarifaRepository tarifaRepository;

    @InjectMocks
    private TarifaService tarifaService;

    private TarifaEntity tarifa;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        tarifa = new TarifaEntity();
        tarifa.setId(1L);
        tarifa.setNumeroVueltas(10);
        tarifa.setTiempoMaximo(15);
        tarifa.setPrecioBase(12000);
        tarifa.setDuracionReserva(20);
    }

    @Test
    void testGetAllTarifas() {
        when(tarifaRepository.findAll()).thenReturn(List.of(tarifa));
        List<TarifaEntity> tarifas = tarifaService.getAllTarifas();
        assertEquals(1, tarifas.size());
        assertEquals(10, tarifas.get(0).getNumeroVueltas());
    }

    @Test
    void testGetTarifaById() {
        when(tarifaRepository.findById(1L)).thenReturn(Optional.of(tarifa));
        Optional<TarifaEntity> result = tarifaService.getTarifaById(1L);
        assertTrue(result.isPresent());
        assertEquals(15, result.get().getTiempoMaximo());
    }

    @Test
    void testGetTarifaByNumeroVueltas() {
        when(tarifaRepository.findByNumeroVueltas(10)).thenReturn(Optional.of(tarifa));
        Optional<TarifaEntity> result = tarifaService.getTarifaByNumeroVueltas(10);
        assertTrue(result.isPresent());
        assertEquals(12000, result.get().getPrecioBase());
    }

    @Test
    void testGetTarifaByNumeroVueltasAndTiempoMaximo() {
        when(tarifaRepository.findByNumeroVueltasAndTiempoMaximo(10, 15)).thenReturn(Optional.of(tarifa));
        Optional<TarifaEntity> result = tarifaService.getTarifaByNumeroVueltasAndTiempoMaximo(10, 15);
        assertTrue(result.isPresent());
        assertEquals(20, result.get().getDuracionReserva());
    }

    @Test
    void testCrearTarifa() {
        when(tarifaRepository.save(any())).thenReturn(tarifa);

        TarifaEntity creada = tarifaService.crearTarifa(10, 15, 12000, 20);
        assertEquals(10, creada.getNumeroVueltas());
        assertEquals(12000, creada.getPrecioBase());
    }

    @Test
    void testActualizarTarifaExistente() {
        TarifaEntity actualizada = new TarifaEntity();
        actualizada.setNumeroVueltas(12);
        actualizada.setTiempoMaximo(20);
        actualizada.setPrecioBase(14000);
        actualizada.setDuracionReserva(25);

        when(tarifaRepository.findById(1L)).thenReturn(Optional.of(tarifa));
        when(tarifaRepository.save(any())).thenReturn(tarifa);

        TarifaEntity result = tarifaService.actualizarTarifa(1L, actualizada);
        assertNotNull(result);
        verify(tarifaRepository).save(any(TarifaEntity.class));
    }

    @Test
    void testActualizarTarifaNoExistente() {
        when(tarifaRepository.findById(99L)).thenReturn(Optional.empty());

        TarifaEntity actualizada = new TarifaEntity();
        TarifaEntity result = tarifaService.actualizarTarifa(99L, actualizada);

        assertNull(result);
    }

    @Test
    void testDeleteTarifa() {
        tarifaService.deleteTarifa(1L);
        verify(tarifaRepository).deleteById(1L);
    }
}
