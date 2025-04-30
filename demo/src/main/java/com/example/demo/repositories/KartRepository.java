package com.example.demo.repositories;

import com.example.demo.entities.KartEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KartRepository extends JpaRepository<KartEntity, String> {

    Optional<KartEntity> findByCodigo(String codigo);

    List<KartEntity> findByEstado(String estado);

    @Query("SELECT k FROM KartEntity k WHERE k.estado = 'Disponible'")
    List<KartEntity> findKartsDisponibles();

    @Query("SELECT COUNT(k) FROM KartEntity k WHERE k.estado = 'Disponible'")
    Integer contarKartsDisponibles();
}