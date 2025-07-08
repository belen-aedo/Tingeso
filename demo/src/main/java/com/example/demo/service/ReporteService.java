package com.example.demo.service;

import com.example.demo.entities.ReporteEntity;
import com.example.demo.entities.ReporteMensualEntity;
import com.example.demo.repositories.ReporteRepository;
import com.example.demo.repositories.ReporteMensualRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ReporteService {

    @Autowired
    private ReporteRepository reporteRepository;

    @Autowired
    private ReporteMensualRepository reporteMensualRepository;

    @Transactional
    public ReporteEntity guardarReporte(ReporteEntity reporte) {
        // Guardar el reporte individual
        ReporteEntity reporteGuardado = reporteRepository.save(reporte);

        // Actualizar el reporte mensual
        actualizarReporteMensual(reporte, "AGREGAR");

        return reporteGuardado;
    }

    @Transactional
    public void eliminarReporte(Long id) {
        Optional<ReporteEntity> reporteOpt = reporteRepository.findById(id);
        if (reporteOpt.isPresent()) {
            ReporteEntity reporte = reporteOpt.get();

            // Eliminar el reporte individual
            reporteRepository.deleteById(id);

            // Actualizar el reporte mensual
            actualizarReporteMensual(reporte, "ELIMINAR");
        }
    }

    private void actualizarReporteMensual(ReporteEntity reporte, String operacion) {
        int mes = reporte.getFecha().getMonthValue();
        int anio = reporte.getFecha().getYear();

        Optional<ReporteMensualEntity> reporteMensualOpt =
                reporteMensualRepository.findByTipoReporteAndMesAndAnio(reporte.getTipoReporte(), mes, anio);

        ReporteMensualEntity reporteMensual;

        if (reporteMensualOpt.isPresent()) {
            reporteMensual = reporteMensualOpt.get();
        } else {
            reporteMensual = new ReporteMensualEntity();
            reporteMensual.setTipoReporte(reporte.getTipoReporte());
            reporteMensual.setMes(mes);
            reporteMensual.setAnio(anio);
            reporteMensual.setIngresoTotal(0.0);
            reporteMensual.setCantidadReportes(0);
            reporteMensual.setTotalVueltas(0);
            reporteMensual.setTiempoMaximoPromedio(0);
            reporteMensual.setTotalPersonasMin(0);
            reporteMensual.setTotalPersonasMax(0);
        }

        if ("AGREGAR".equals(operacion)) {
            reporteMensual.setIngresoTotal(reporteMensual.getIngresoTotal() + reporte.getIngresoTotal());
            reporteMensual.setCantidadReportes(reporteMensual.getCantidadReportes() + 1);

            if ("PorVueltas".equals(reporte.getTipoReporte()) && reporte.getNumeroVueltas() != null) {
                reporteMensual.setTotalVueltas(
                        (reporteMensual.getTotalVueltas() != null ? reporteMensual.getTotalVueltas() : 0) +
                                reporte.getNumeroVueltas()
                );
            }

            if ("PorPersonas".equals(reporte.getTipoReporte())) {
                if (reporte.getMinPersonas() != null) {
                    reporteMensual.setTotalPersonasMin(
                            (reporteMensual.getTotalPersonasMin() != null ? reporteMensual.getTotalPersonasMin() : 0) +
                                    reporte.getMinPersonas()
                    );
                }
                if (reporte.getMaxPersonas() != null) {
                    reporteMensual.setTotalPersonasMax(
                            (reporteMensual.getTotalPersonasMax() != null ? reporteMensual.getTotalPersonasMax() : 0) +
                                    reporte.getMaxPersonas()
                    );
                }
            }
        } else if ("ELIMINAR".equals(operacion)) {
            reporteMensual.setIngresoTotal(reporteMensual.getIngresoTotal() - reporte.getIngresoTotal());
            reporteMensual.setCantidadReportes(reporteMensual.getCantidadReportes() - 1);

            if ("PorVueltas".equals(reporte.getTipoReporte()) && reporte.getNumeroVueltas() != null) {
                reporteMensual.setTotalVueltas(
                        (reporteMensual.getTotalVueltas() != null ? reporteMensual.getTotalVueltas() : 0) -
                                reporte.getNumeroVueltas()
                );
            }

            if ("PorPersonas".equals(reporte.getTipoReporte())) {
                if (reporte.getMinPersonas() != null) {
                    reporteMensual.setTotalPersonasMin(
                            (reporteMensual.getTotalPersonasMin() != null ? reporteMensual.getTotalPersonasMin() : 0) -
                                    reporte.getMinPersonas()
                    );
                }
                if (reporte.getMaxPersonas() != null) {
                    reporteMensual.setTotalPersonasMax(
                            (reporteMensual.getTotalPersonasMax() != null ? reporteMensual.getTotalPersonasMax() : 0) -
                                    reporte.getMaxPersonas()
                    );
                }
            }
        }

        reporteMensual.setFechaActualizacion(LocalDate.now());

        // Si después de eliminar no quedan reportes, eliminar el reporte mensual
        if (reporteMensual.getCantidadReportes() <= 0 && "ELIMINAR".equals(operacion)) {
            if (reporteMensualOpt.isPresent()) {
                reporteMensualRepository.delete(reporteMensual);
            }
        } else {
            reporteMensualRepository.save(reporteMensual);
        }
    }

    // Métodos para reportes individuales
    public List<ReporteEntity> obtenerTodos() {
        return reporteRepository.findAll();
    }

    public List<ReporteEntity> obtenerPorTipo(String tipoReporte) {
        return reporteRepository.findByTipoReporte(tipoReporte);
    }

    public List<ReporteEntity> obtenerPorFecha(LocalDate fecha) {
        return reporteRepository.findByFecha(fecha);
    }

    public List<ReporteEntity> obtenerPorMesYAnio(int mes, int anio) {
        return reporteRepository.findByMesYAnio(mes, anio);
    }

    public Optional<ReporteEntity> obtenerPorId(Long id) {
        return reporteRepository.findById(id);
    }

    // Métodos para reportes mensuales
    public List<ReporteMensualEntity> obtenerReportesMensuales() {
        return reporteMensualRepository.findAll();
    }

    public List<ReporteMensualEntity> obtenerReportesMensualesPorTipo(String tipoReporte) {
        return reporteMensualRepository.findByTipoReporte(tipoReporte);
    }

    public List<ReporteMensualEntity> obtenerReportesMensualesPorMesYAnio(int mes, int anio) {
        return reporteMensualRepository.findByMesAndAnio(mes, anio);
    }

    public List<ReporteMensualEntity> obtenerReportesMensualesPorAnio(int anio) {
        return reporteMensualRepository.findByAnio(anio);
    }


}