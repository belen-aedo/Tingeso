package com.example.demo.service;

import com.example.demo.entities.TarifaEntity;
import com.example.demo.repositories.TarifaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class TarifaService {
    private final TarifaRepository tarifaRepository;

    @Autowired
    public TarifaService(TarifaRepository tarifaRepository) {
        this.tarifaRepository = tarifaRepository;
    }

    public List<TarifaEntity> getAllTarifas() {
        return tarifaRepository.findAll();
    }

    public List<TarifaEntity> getTarifasOrderedByPrecio() {
        return tarifaRepository.findAllOrderByPrecioBaseAsc();
    }

    public Optional<TarifaEntity> getTarifaById(Long id) {
        return tarifaRepository.findById(id);
    }

    public Optional<TarifaEntity> getTarifaByNumeroVueltas(int numeroVueltas) {
        return tarifaRepository.findByNumeroVueltas(numeroVueltas);
    }

    public Optional<TarifaEntity> getTarifaByNumeroVueltasAndTiempoMaximo(int numeroVueltas, int tiempoMaximo) {
        return tarifaRepository.findByNumeroVueltasAndTiempoMaximo(numeroVueltas, tiempoMaximo);
    }

    public List<TarifaEntity> getTarifasByRangoPrecio(int precioMin, int precioMax) {
        return tarifaRepository.findByRangoPrecio(precioMin, precioMax);
    }

    public TarifaEntity saveTarifa(TarifaEntity tarifa) {
        return tarifaRepository.save(tarifa);
    }

    public void deleteTarifa(Long id) {
        tarifaRepository.deleteById(id);
    }

    /**
     * Crea una nueva tarifa con los parámetros especificados
     *
     * @param numeroVueltas Número de vueltas de la tarifa
     * @param tiempoMaximo Tiempo máximo en minutos
     * @param precioBase Precio base de la tarifa
     * @param duracionReserva Duración de la reserva en minutos
     * @return La tarifa creada y guardada
     */
    public TarifaEntity crearTarifa(int numeroVueltas, int tiempoMaximo, int precioBase, int duracionReserva) {
        TarifaEntity nuevaTarifa = new TarifaEntity();
        nuevaTarifa.setNumeroVueltas(numeroVueltas);
        nuevaTarifa.setTiempoMaximo(tiempoMaximo);
        nuevaTarifa.setPrecioBase(precioBase);
        nuevaTarifa.setDuracionReserva(duracionReserva);

        return tarifaRepository.save(nuevaTarifa);
    }

    /**
     * Actualiza una tarifa existente
     *
     * @param id ID de la tarifa a actualizar
     * @param tarifaActualizada Objeto con los nuevos valores
     * @return La tarifa actualizada o null si no se encuentra
     */
    public TarifaEntity actualizarTarifa(Long id, TarifaEntity tarifaActualizada) {
        Optional<TarifaEntity> tarifaExistente = tarifaRepository.findById(id);

        if (tarifaExistente.isPresent()) {
            TarifaEntity tarifa = tarifaExistente.get();
            tarifa.setNumeroVueltas(tarifaActualizada.getNumeroVueltas());
            tarifa.setTiempoMaximo(tarifaActualizada.getTiempoMaximo());
            tarifa.setPrecioBase(tarifaActualizada.getPrecioBase());
            tarifa.setDuracionReserva(tarifaActualizada.getDuracionReserva());

            return tarifaRepository.save(tarifa);
        }

        return null;
    }

}