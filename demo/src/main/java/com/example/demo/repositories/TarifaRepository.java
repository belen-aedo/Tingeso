package com.example.demo.repositories;

import com.example.demo.entities.TarifaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TarifaRepository extends JpaRepository<TarifaEntity, Long> {
    Optional<TarifaEntity> findByNumeroVueltas(int numeroVueltas);

    Optional<TarifaEntity> findByDuracionReserva(int duracionReserva);

    @Query("SELECT t FROM TarifaEntity t WHERE t.numeroVueltas = :numeroVueltas AND t.tiempoMaximo = :tiempoMaximo")
    Optional<TarifaEntity> findByNumeroVueltasAndTiempoMaximo(int numeroVueltas, int tiempoMaximo);

    @Query("SELECT t FROM TarifaEntity t ORDER BY t.precioBase ASC")
    List<TarifaEntity> findAllOrderByPrecioBaseAsc();

    @Query("SELECT t FROM TarifaEntity t WHERE t.precioBase BETWEEN :precioMin AND :precioMax")
    List<TarifaEntity> findByRangoPrecio(int precioMin, int precioMax);

    /**
     * Busca la tarifa con el tiempo máximo más pequeño que sea mayor o igual al tiempo proporcionado
     * @param tiempo El tiempo mínimo requerido
     * @return La primera tarifa que cumple la condición o null si no hay coincidencias
     */
    TarifaEntity findFirstByTiempoMaximoGreaterThanEqualOrderByTiempoMaximoAsc(int tiempo);

    TarifaEntity findFirstByOrderByTiempoMaximoDesc();
}