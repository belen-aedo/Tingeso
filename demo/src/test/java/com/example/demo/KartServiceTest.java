package com.example.demo.service;

import com.example.demo.entities.KartEntity;
import com.example.demo.repositories.KartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KartServiceTest {

    @Mock
    private KartRepository kartRepository;

    @InjectMocks
    private KartService kartService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllKarts_returnsList() {
        List<KartEntity> karts = Arrays.asList(new KartEntity(), new KartEntity());
        when(kartRepository.findAll()).thenReturn(karts);

        List<KartEntity> result = kartService.getAllKarts();

        assertEquals(2, result.size());
        verify(kartRepository).findAll();
    }

    @Test
    void getKartByCodigo_found() {
        KartEntity kart = new KartEntity();
        kart.setCodigo("K001");
        when(kartRepository.findByCodigo("K001")).thenReturn(Optional.of(kart));

        Optional<KartEntity> result = kartService.getKartByCodigo("K001");

        assertTrue(result.isPresent());
        assertEquals("K001", result.get().getCodigo());
        verify(kartRepository).findByCodigo("K001");
    }

    @Test
    void getKartByCodigo_notFound() {
        when(kartRepository.findByCodigo("K999")).thenReturn(Optional.empty());

        Optional<KartEntity> result = kartService.getKartByCodigo("K999");

        assertFalse(result.isPresent());
        verify(kartRepository).findByCodigo("K999");
    }

    @Test
    void getKartsByEstado_returnsList() {
        List<KartEntity> karts = Collections.singletonList(new KartEntity());
        when(kartRepository.findByEstado("Disponible")).thenReturn(karts);

        List<KartEntity> result = kartService.getKartsByEstado("Disponible");

        assertEquals(1, result.size());
        verify(kartRepository).findByEstado("Disponible");
    }

    @Test
    void getKartsDisponibles_returnsList() {
        List<KartEntity> karts = Collections.singletonList(new KartEntity());
        when(kartRepository.findKartsDisponibles()).thenReturn(karts);

        List<KartEntity> result = kartService.getKartsDisponibles();

        assertEquals(1, result.size());
        verify(kartRepository).findKartsDisponibles();
    }

    @Test
    void contarKartsDisponibles_returnsCount() {
        when(kartRepository.contarKartsDisponibles()).thenReturn(5);

        Integer count = kartService.contarKartsDisponibles();

        assertEquals(5, count);
        verify(kartRepository).contarKartsDisponibles();
    }

    @Test
    void saveKart_savesAndReturnsKart() {
        KartEntity kart = new KartEntity();
        kart.setCodigo("K001");
        when(kartRepository.save(kart)).thenReturn(kart);

        KartEntity result = kartService.saveKart(kart);

        assertEquals("K001", result.getCodigo());
        verify(kartRepository).save(kart);
    }

    @Test
    void deleteKart_successfulDeletion() {
        String codigo = "K001";
        when(kartRepository.existsById(codigo)).thenReturn(true);

        kartService.deleteKart(codigo);

        verify(kartRepository).existsById(codigo);
        verify(kartRepository).deleteById(codigo);
    }

    @Test
    void deleteKart_kartNotFound_throwsException() {
        String codigo = "K999";
        when(kartRepository.existsById(codigo)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> kartService.deleteKart(codigo));

        assertEquals("Kart con código K999 no encontrado", exception.getMessage());
        verify(kartRepository).existsById(codigo);
        verify(kartRepository, never()).deleteById(anyString());
    }

    @Test
    void cambiarEstadoKart_kartFound_updatesAndReturnsKart() {
        String codigo = "K001";
        KartEntity kart = new KartEntity();
        kart.setCodigo(codigo);
        kart.setEstado("Disponible");

        when(kartRepository.findById(codigo)).thenReturn(Optional.of(kart));
        when(kartRepository.save(any(KartEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<KartEntity> result = kartService.cambiarEstadoKart(codigo, "En uso");

        assertTrue(result.isPresent());
        assertEquals("En uso", result.get().getEstado());
        verify(kartRepository).findById(codigo);
        verify(kartRepository).save(kart);
    }

    @Test
    void cambiarEstadoKart_kartNotFound_returnsEmpty() {
        String codigo = "K999";
        when(kartRepository.findById(codigo)).thenReturn(Optional.empty());

        Optional<KartEntity> result = kartService.cambiarEstadoKart(codigo, "En uso");

        assertFalse(result.isPresent());
        verify(kartRepository).findById(codigo);
        verify(kartRepository, never()).save(any());
    }

    @Test
    void inicializarKarts_whenRepositoryHasData_doesNothing() {
        when(kartRepository.count()).thenReturn(5L);

        kartService.inicializarKarts();

        verify(kartRepository).count();
        verify(kartRepository, never()).save(any());
    }

    @Test
    void inicializarKarts_whenRepositoryEmpty_inserts15Karts() {
        when(kartRepository.count()).thenReturn(0L);

        kartService.inicializarKarts();

        verify(kartRepository).count();
        verify(kartRepository, times(15)).save(any(KartEntity.class));
    }
}
