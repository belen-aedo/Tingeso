package com.example.demo.service;

import com.example.demo.entities.TarifaEntity;
import com.example.demo.repositories.TarifaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class TarifaServiceTest {

    @Autowired
    private TarifaService tarifaService;

    @Autowired
    private TarifaRepository tarifaRepository;

    private TarifaEntity tarifa;

    @BeforeEach
    void setup() {
        tarifaRepository.deleteAll();
        tarifa = new TarifaEntity();
        tarifa.setNumeroVueltas(10);
        tarifa.setTiempoMaximo(15);
        tarifa.setPrecioBase(5000);
        tarifa.setDuracionReserva(30);
        tarifaRepository.save(tarifa);
    }

    @Test
    void whenGetAllTarifas_thenReturnList() {
        List<TarifaEntity> tarifas = tarifaService.getAllTarifas();
        assertThat(tarifas).hasSize(1);
    }

    @Test
    void whenGetTarifasOrderedByPrecio_thenReturnOrderedList() {
        List<TarifaEntity> tarifas = tarifaService.getTarifasOrderedByPrecio();
        assertThat(tarifas).isNotEmpty();
        assertThat(tarifas.get(0).getPrecioBase()).isEqualTo(5000);
    }

    @Test
    void whenGetTarifaById_thenReturnTarifa() {
        Optional<TarifaEntity> found = tarifaService.getTarifaById(tarifa.getId());
        assertThat(found).isPresent();
    }

    @Test
    void whenGetTarifaByNumeroVueltas_thenReturnOptional() {
        Optional<TarifaEntity> found = tarifaService.getTarifaByNumeroVueltas(10);
        assertThat(found).isPresent();
    }

    @Test
    void whenGetTarifaByNumeroVueltasAndTiempoMaximo_thenReturnOptional() {
        Optional<TarifaEntity> found = tarifaService.getTarifaByNumeroVueltasAndTiempoMaximo(10, 15);
        assertThat(found).isPresent();
    }

    @Test
    void whenGetTarifasByRangoPrecio_thenReturnList() {
        List<TarifaEntity> tarifas = tarifaService.getTarifasByRangoPrecio(4000, 6000);
        assertThat(tarifas).hasSize(1);
    }

    @Test
    void whenSaveTarifa_thenItIsSaved() {
        TarifaEntity nueva = new TarifaEntity();
        nueva.setNumeroVueltas(5);
        nueva.setTiempoMaximo(10);
        nueva.setPrecioBase(3000);
        nueva.setDuracionReserva(20);

        TarifaEntity saved = tarifaService.saveTarifa(nueva);
        assertThat(saved.getPrecioBase()).isEqualTo(3000);
    }

    @Test
    void whenDeleteTarifa_thenItIsRemoved() {
        tarifaService.deleteTarifa(tarifa.getId());
        Optional<TarifaEntity> deleted = tarifaRepository.findById(tarifa.getId());
        assertThat(deleted).isEmpty();
    }

    @Test
    void whenCrearTarifa_thenItIsCreatedAndSaved() {
        TarifaEntity created = tarifaService.crearTarifa(8, 20, 7000, 25);
        assertThat(created.getNumeroVueltas()).isEqualTo(8);
        assertThat(created.getTiempoMaximo()).isEqualTo(20);
        assertThat(created.getPrecioBase()).isEqualTo(7000);
        assertThat(created.getDuracionReserva()).isEqualTo(25);
    }

    @Test
    void whenActualizarTarifa_thenValuesUpdated() {
        TarifaEntity actualizada = new TarifaEntity();
        actualizada.setNumeroVueltas(12);
        actualizada.setTiempoMaximo(18);
        actualizada.setPrecioBase(6000);
        actualizada.setDuracionReserva(35);

        TarifaEntity updated = tarifaService.actualizarTarifa(tarifa.getId(), actualizada);
        assertThat(updated).isNotNull();
        assertThat(updated.getNumeroVueltas()).isEqualTo(12);
    }

    @Test
    void whenActualizarTarifaNonExistent_thenReturnNull() {
        TarifaEntity actualizada = new TarifaEntity();
        TarifaEntity updated = tarifaService.actualizarTarifa(999L, actualizada);
        assertThat(updated).isNull();
    }
}
