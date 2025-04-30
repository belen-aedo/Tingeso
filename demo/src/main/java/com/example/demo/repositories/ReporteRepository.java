package com.example.demo.repositories;

import com.example.demo.entities.ReporteEntity;
import org.springframework.cglib.core.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReporteRepository extends JpaRepository<ReporteEntity, Long> {
    List<ReporteEntity> findByTipoReporte(String tipoReporte);

    List<ReporteEntity> findByMesGenerado(LocalDate mesGenerado);

    Optional<ReporteEntity> findByTipoReporteAndMesGenerado(String tipoReporte, LocalDate mesGenerado);

    @Query("SELECT r FROM ReporteEntity r WHERE r.tipoReporte = 'PorVueltas' AND r.numeroVueltas = :numeroVueltas AND r.mesGenerado = :mesGenerado")
    Optional<ReporteEntity> findReportePorVueltasYMes(int numeroVueltas, LocalDate mesGenerado);

    @Query("SELECT r FROM ReporteEntity r WHERE r.tipoReporte = 'PorPersonas' AND r.minPersonas <= :personas AND r.maxPersonas >= :personas AND r.mesGenerado = :mesGenerado")
    List<ReporteEntity> findReportesPorRangoPersonasYMes(int personas, LocalDate mesGenerado);


}