package com.example.demo.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "reportes_mensuales")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteMensualEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    private String tipoReporte;  // "PorVueltas", "PorPersonas"
    private int mes;             // 1-12
    private int anio;            // 2024, 2025, etc.
    private double ingresoTotal; // Suma de todos los reportes del mes
    private int cantidadReportes; // Cantidad de reportes individuales

    // Para reportes por vueltas
    private Integer totalVueltas;
    private Integer tiempoMaximoPromedio;

    // Para reportes por personas
    private Integer totalPersonasMin;
    private Integer totalPersonasMax;

    // Fecha de última actualización
    private LocalDate fechaActualizacion;
}