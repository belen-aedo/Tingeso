
package com.example.demo;

import com.example.demo.entities.KartEntity;
import com.example.demo.repositories.KartRepository;
import com.example.demo.service.KartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KartServiceTest {

    @Mock
    private KartRepository kartRepository;

    @InjectMocks
    private KartService kartService;

    private KartEntity kartEntity;
    private List<KartEntity> kartList;

    @BeforeEach
    void setUp() {
        // Preparar datos de prueba
        kartEntity = new KartEntity();
        kartEntity.setCodigo("K001");
        kartEntity.setEstado("Disponible");
        kartEntity.setModelo("Sodikart RT8");

        KartEntity kart2 = new KartEntity();
        kart2.setCodigo("K002");
        kart2.setEstado("En uso");
        kart2.setModelo("Sodikart RT8");

        KartEntity kart3 = new KartEntity();
        kart3.setCodigo("K003");
        kart3.setEstado("En mantenimiento");
        kart3.setModelo("Sodikart RT8");

        kartList = Arrays.asList(kartEntity, kart2, kart3);
    }

    @Test
    void getAllKarts_ReturnsList() {
        // Given
        when(kartRepository.findAll()).thenReturn(kartList);

        // When
        List<KartEntity> result = kartService.getAllKarts();

        // Then
        assertEquals(3, result.size());
        verify(kartRepository, times(1)).findAll();
    }

    @Test
    void getKartByCodigo_ExistingKart_ReturnsKart() {
        // Given
        when(kartRepository.findByCodigo("K001")).thenReturn(Optional.of(kartEntity));

        // When
        Optional<KartEntity> result = kartService.getKartByCodigo("K001");

        // Then
        assertTrue(result.isPresent());
        assertEquals("K001", result.get().getCodigo());
        verify(kartRepository, times(1)).findByCodigo("K001");
    }

    @Test
    void getKartByCodigo_NonExistingKart_ReturnsEmpty() {
        // Given
        when(kartRepository.findByCodigo("K999")).thenReturn(Optional.empty());

        // When
        Optional<KartEntity> result = kartService.getKartByCodigo("K999");

        // Then
        assertFalse(result.isPresent());
        verify(kartRepository, times(1)).findByCodigo("K999");
    }

    @Test
    void getKartsByEstado_WithMatches_ReturnsList() {
        // Given
        List<KartEntity> disponibles = List.of(kartEntity);
        when(kartRepository.findByEstado("Disponible")).thenReturn(disponibles);

        // When
        List<KartEntity> result = kartService.getKartsByEstado("Disponible");

        // Then
        assertEquals(1, result.size());
        assertEquals("Disponible", result.get(0).getEstado());
        verify(kartRepository, times(1)).findByEstado("Disponible");
    }

    @Test
    void getKartsDisponibles_ReturnsDisponibleKarts() {
        // Given
        List<KartEntity> disponibles = List.of(kartEntity);
        when(kartRepository.findKartsDisponibles()).thenReturn(disponibles);

        // When
        List<KartEntity> result = kartService.getKartsDisponibles();

        // Then
        assertEquals(1, result.size());
        assertEquals("Disponible", result.get(0).getEstado());
        verify(kartRepository, times(1)).findKartsDisponibles();
    }

    @Test
    void contarKartsDisponibles_ReturnsCount() {
        // Given
        when(kartRepository.contarKartsDisponibles()).thenReturn(5);

        // When
        Integer result = kartService.contarKartsDisponibles();

        // Then
        assertEquals(5, result);
        verify(kartRepository, times(1)).contarKartsDisponibles();
    }

    @Test
    void saveKart_ReturnsSavedKart() {
        // Given
        when(kartRepository.save(any(KartEntity.class))).thenReturn(kartEntity);

        // When
        KartEntity result = kartService.saveKart(kartEntity);

        // Then
        assertNotNull(result);
        assertEquals("K001", result.getCodigo());
        verify(kartRepository, times(1)).save(kartEntity);
    }

    @Test
    void deleteKart_CallsRepositoryDelete() {
        // When
        kartService.deleteKart("K001");

        // Then
        verify(kartRepository, times(1)).deleteById("K001");
    }

    @Test
    void cambiarEstadoKart_ExistingKart_ReturnsUpdatedKart() {
        // Given
        when(kartRepository.findById("K001")).thenReturn(Optional.of(kartEntity));
        KartEntity updatedKart = new KartEntity();
        updatedKart.setCodigo("K001");
        updatedKart.setEstado("En uso");
        updatedKart.setModelo("Sodikart RT8");
        when(kartRepository.save(any(KartEntity.class))).thenReturn(updatedKart);

        // When
        Optional<KartEntity> result = kartService.cambiarEstadoKart("K001", "En uso");

        // Then
        assertTrue(result.isPresent());
        assertEquals("En uso", result.get().getEstado());
        verify(kartRepository, times(1)).findById("K001");
        verify(kartRepository, times(1)).save(any(KartEntity.class));
    }

    @Test
    void cambiarEstadoKart_NonExistingKart_ReturnsEmpty() {
        // Given
        when(kartRepository.findById("K999")).thenReturn(Optional.empty());

        // When
        Optional<KartEntity> result = kartService.cambiarEstadoKart("K999", "En uso");

        // Then
        assertFalse(result.isPresent());
        verify(kartRepository, times(1)).findById("K999");
        verify(kartRepository, never()).save(any(KartEntity.class));
    }

    @Test
    void inicializarKarts_WhenKartsExist_DoesNothing() {
        // Given
        when(kartRepository.count()).thenReturn(15L);

        // When
        kartService.inicializarKarts();

        // Then
        verify(kartRepository, times(1)).count();
        verify(kartRepository, never()).save(any(KartEntity.class));
    }

    @Test
    void inicializarKarts_WhenNoKarts_Creates15Karts() {
        // Given
        when(kartRepository.count()).thenReturn(0L);
        when(kartRepository.save(any(KartEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        kartService.inicializarKarts();

        // Then
        verify(kartRepository, times(1)).count();
        verify(kartRepository, times(15)).save(any(KartEntity.class));
    }
}