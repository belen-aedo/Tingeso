package com.example.demo.service;

import com.example.demo.entities.ReporteEntity;
import com.example.demo.entities.ReporteMensualEntity;
import com.example.demo.repositories.ReporteRepository;
import com.example.demo.repositories.ReporteMensualRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ReporteService {




    private static final String ELIMINAR = "ELIMINAR";
    private static final String AGREGAR = "AGREGAR";
    private static final String POR_VUELTAS = "PorVueltas";
    private static final String POR_PERSONAS = "PorPersonas";
    private final ReporteMensualRepository reporteMensualRepository;
    private final ReporteRepository reporteRepository;
    public ReporteService(ReporteRepository reporteRepository,
                          ReporteMensualRepository reporteMensualRepository) {
        this.reporteRepository = reporteRepository;
        this.reporteMensualRepository = reporteMensualRepository;
    }

    // Excepciones específicas
    public static class ReporteException extends RuntimeException {
        public ReporteException(String message) {
            super(message);
        }

        public ReporteException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class ReporteNoEncontradoException extends ReporteException {
        public ReporteNoEncontradoException(String message) {
            super(message);
        }
    }

    @Transactional
    public ReporteEntity guardarReporte(ReporteEntity reporte) {
        // Guardar el reporte individual
        ReporteEntity reporteGuardado = reporteRepository.save(reporte);

        // Actualizar el reporte mensual
        actualizarReporteMensual(reporte, AGREGAR);

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
            actualizarReporteMensual(reporte, ELIMINAR);
        }
    }

    private void actualizarReporteMensual(ReporteEntity reporte, String operacion) {
        int mes = reporte.getFecha().getMonthValue();
        int anio = reporte.getFecha().getYear();

        Optional<ReporteMensualEntity> reporteMensualOpt =
                reporteMensualRepository.findByTipoReporteAndMesAndAnio(reporte.getTipoReporte(), mes, anio);

        ReporteMensualEntity reporteMensual = obtenerOCrearReporteMensual(reporteMensualOpt, reporte, mes, anio);

        if (AGREGAR.equals(operacion)) {
            procesarOperacionAgregar(reporteMensual, reporte);
        } else if (ELIMINAR.equals(operacion)) {
            procesarOperacionEliminar(reporteMensual, reporte);
        }

        reporteMensual.setFechaActualizacion(LocalDate.now());

        // Si después de eliminar no quedan reportes, eliminar el reporte mensual
        if (reporteMensual.getCantidadReportes() <= 0 && ELIMINAR.equals(operacion)) {
            if (reporteMensualOpt.isPresent()) {
                reporteMensualRepository.delete(reporteMensual);
            }
        } else {
            reporteMensualRepository.save(reporteMensual);
        }
    }

    private ReporteMensualEntity obtenerOCrearReporteMensual(Optional<ReporteMensualEntity> reporteMensualOpt,
                                                             ReporteEntity reporte, int mes, int anio) {
        if (reporteMensualOpt.isPresent()) {
            return reporteMensualOpt.get();
        }

        ReporteMensualEntity reporteMensual = new ReporteMensualEntity();
        reporteMensual.setTipoReporte(reporte.getTipoReporte());
        reporteMensual.setMes(mes);
        reporteMensual.setAnio(anio);
        reporteMensual.setIngresoTotal(0.0);
        reporteMensual.setCantidadReportes(0);
        reporteMensual.setTotalVueltas(0);
        reporteMensual.setTiempoMaximoPromedio(0);
        reporteMensual.setTotalPersonasMin(0);
        reporteMensual.setTotalPersonasMax(0);

        return reporteMensual;
    }

    private void procesarOperacionAgregar(ReporteMensualEntity reporteMensual, ReporteEntity reporte) {
        reporteMensual.setIngresoTotal(reporteMensual.getIngresoTotal() + reporte.getIngresoTotal());
        reporteMensual.setCantidadReportes(reporteMensual.getCantidadReportes() + 1);

        if (POR_VUELTAS.equals(reporte.getTipoReporte()) && reporte.getNumeroVueltas() != null) {
            procesarVueltasAgregar(reporteMensual, reporte);
        }

        if (POR_PERSONAS.equals(reporte.getTipoReporte())) {
            procesarPersonasAgregar(reporteMensual, reporte);
        }
    }

    private void procesarVueltasAgregar(ReporteMensualEntity reporteMensual, ReporteEntity reporte) {
        reporteMensual.setTotalVueltas(
                (reporteMensual.getTotalVueltas() != null ? reporteMensual.getTotalVueltas() : 0) +
                        reporte.getNumeroVueltas()
        );
    }

    private void procesarPersonasAgregar(ReporteMensualEntity reporteMensual, ReporteEntity reporte) {
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

    private void procesarOperacionEliminar(ReporteMensualEntity reporteMensual, ReporteEntity reporte) {
        reporteMensual.setIngresoTotal(reporteMensual.getIngresoTotal() - reporte.getIngresoTotal());
        reporteMensual.setCantidadReportes(reporteMensual.getCantidadReportes() - 1);

        if (POR_VUELTAS.equals(reporte.getTipoReporte()) && reporte.getNumeroVueltas() != null) {
            procesarVueltasEliminar(reporteMensual, reporte);
        }

        if (POR_PERSONAS.equals(reporte.getTipoReporte())) {
            procesarPersonasEliminar(reporteMensual, reporte);
        }
    }

    private void procesarVueltasEliminar(ReporteMensualEntity reporteMensual, ReporteEntity reporte) {
        reporteMensual.setTotalVueltas(
                (reporteMensual.getTotalVueltas() != null ? reporteMensual.getTotalVueltas() : 0) -
                        reporte.getNumeroVueltas()
        );
    }

    private void procesarPersonasEliminar(ReporteMensualEntity reporteMensual, ReporteEntity reporte) {
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

    // Métodos para reportes individuales
    public List<ReporteEntity> obtenerTodos() {
        return reporteRepository.findAll();
    }

    public List<ReporteEntity> obtenerPorTipo(String tipoReporte) {
        return reporteRepository.findByTipoReporte(tipoReporte);
    }

    public Optional<ReporteEntity> obtenerPorFecha(LocalDate fecha) {
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

    public ReporteEntity obtenerPorMes(LocalDate mes) {
        return reporteRepository.findByMesGenerado(mes)
                .orElseThrow(() -> new ReporteNoEncontradoException("No se encontró un reporte para el mes: " + mes));
    }
}