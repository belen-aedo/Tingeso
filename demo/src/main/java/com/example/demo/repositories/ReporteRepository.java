package com.example.demo.repositories;

import com.example.demo.entities.ReporteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReporteRepository extends JpaRepository<ReporteEntity, Long> {
    List<ReporteEntity> findByTipoReporte(String tipoReporte);

    List<ReporteEntity> findByFecha(LocalDate fecha);

    List<ReporteEntity> findByTipoReporteAndFecha(String tipoReporte, LocalDate fecha);

    @Query("SELECT r FROM ReporteEntity r WHERE MONTH(r.fecha) = :mes AND YEAR(r.fecha) = :anio")
    List<ReporteEntity> findByMesYAnio(int mes, int anio);

    @Query("SELECT r FROM ReporteEntity r WHERE r.tipoReporte = :tipoReporte AND MONTH(r.fecha) = :mes AND YEAR(r.fecha) = :anio")
    List<ReporteEntity> findByTipoReporteAndMesYAnio(String tipoReporte, int mes, int anio);

    @Query("SELECT r FROM ReporteEntity r WHERE r.tipoReporte = 'PorVueltas' AND r.numeroVueltas = :numeroVueltas AND r.fecha = :fecha")
    Optional<ReporteEntity> findReportePorVueltasYFecha(int numeroVueltas, LocalDate fecha);

    @Query("SELECT r FROM ReporteEntity r WHERE r.tipoReporte = 'PorPersonas' AND r.minPersonas <= :personas AND r.maxPersonas >= :personas AND r.fecha = :fecha")
    List<ReporteEntity> findReportesPorRangoPersonasYFecha(int personas, LocalDate fecha);

    Object findByMesGenerado(LocalDate mes);
}