package com.example.demo.service;

import com.example.demo.entities.ReporteEntity;
import com.example.demo.repositories.ReporteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ReporteService {

    @Autowired
    private ReporteRepository reporteRepository;

    public ReporteEntity guardarReporte(ReporteEntity reporte) {
        return reporteRepository.save(reporte);
    }

    public List<ReporteEntity> obtenerTodos() {
        return reporteRepository.findAll();
    }

    public List<ReporteEntity> obtenerPorTipo(String tipoReporte) {
        return reporteRepository.findByTipoReporte(tipoReporte);
    }

    public List<ReporteEntity> obtenerPorMes(LocalDate mes) {
        return reporteRepository.findByMesGenerado(mes);
    }

    public Optional<ReporteEntity> obtenerPorId(Long id) {
        return reporteRepository.findById(id);
    }


    public void eliminarReporte(Long id) {
        reporteRepository.deleteById(id);
    }



}
