package com.example.demo.repositories;

import com.example.demo.entities.ReporteMensualEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReporteMensualRepository extends JpaRepository<ReporteMensualEntity, Long> {
    Optional<ReporteMensualEntity> findByTipoReporteAndMesAndAnio(String tipoReporte, int mes, int anio);

    List<ReporteMensualEntity> findByMesAndAnio(int mes, int anio);

    List<ReporteMensualEntity> findByTipoReporte(String tipoReporte);

    List<ReporteMensualEntity> findByAnio(int anio);
}